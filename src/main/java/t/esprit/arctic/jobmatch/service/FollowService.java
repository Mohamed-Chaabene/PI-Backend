package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;

    /**
     * Add a follower to a user
     * @param followerId The ID of the user who is following
     * @param userToFollowId The ID of the user to be followed
     * @return The updated user with new follower
     */
    public Utilisateur followUser(Long followerId, Long userToFollowId) {
        Utilisateur userToFollow = utilisateurRepository.findById(userToFollowId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String followers = userToFollow.getFollowers();
        if (followers == null || followers.isEmpty()) {
            userToFollow.setFollowers(String.valueOf(followerId));
        } else if (!followers.contains(String.valueOf(followerId))) {
            userToFollow.setFollowers(followers + "," + followerId);
        }

        Utilisateur savedUser = utilisateurRepository.save(userToFollow);

        // Send real-time notification
        notificationService.createFollowNotification(userToFollowId, followerId);

        return savedUser;
    }

    /**
     * Remove a follower from a user
     * @param followerId The ID of the user who is unfollowing
     * @param userToUnfollowId The ID of the user to unfollow
     * @return The updated user with follower removed
     */
    public Utilisateur unfollowUser(Long followerId, Long userToUnfollowId) {
        Utilisateur userToUnfollow = utilisateurRepository.findById(userToUnfollowId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String followers = userToUnfollow.getFollowers();
        if (followers != null && !followers.isEmpty()) {
            String[] followerIds = followers.split(",");
            String newFollowers = Arrays.stream(followerIds)
                    .filter(id -> !id.trim().equals(String.valueOf(followerId)))
                    .collect(Collectors.joining(","));

            userToUnfollow.setFollowers(newFollowers.isEmpty() ? null : newFollowers);
        }

        return utilisateurRepository.save(userToUnfollow);
    }

    /**
     * Get all followers of a user
     * @param userId The ID of the user
     * @return List of users who follow this user
     */
    public List<Utilisateur> getFollowers(Long userId) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String followers = user.getFollowers();

        if (followers == null || followers.isEmpty()) {
            return List.of();
        }

        return Arrays.stream(followers.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .map(id -> utilisateurRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Follower non trouvé")))
                .collect(Collectors.toList());
    }

    /**
     * Check if a user is following another user
     * @param followerId The ID of the user who might be following
     * @param userIdToCheck The ID of the user to check
     * @return true if followerId is in userIdToCheck's followers
     */
    public boolean isFollowing(Long followerId, Long userIdToCheck) {
        Utilisateur user = utilisateurRepository.findById(userIdToCheck)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String followers = user.getFollowers();

        if (followers == null || followers.isEmpty()) {
            return false;
        }

        return followers.contains(String.valueOf(followerId));
    }

    /**
     * Get the count of followers for a user
     * @param userId The ID of the user
     * @return Number of followers
     */
    public int getFollowersCount(Long userId) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String followers = user.getFollowers();

        if (followers == null || followers.isEmpty()) {
            return 0;
        }

        return followers.split(",").length;
    }
}
