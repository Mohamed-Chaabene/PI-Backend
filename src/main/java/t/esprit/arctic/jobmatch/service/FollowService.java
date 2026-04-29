package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;

    private Set<Long> parseFollowerIds(String followers) {
        if (followers == null || followers.isBlank()) {
            return new HashSet<>();
        }
        Set<Long> ids = new HashSet<>();
        for (String s : followers.split(",")) {
            try {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    ids.add(Long.valueOf(trimmed));
                }
            } catch (NumberFormatException e) {
                // Ignore invalid IDs instead of throwing 500 error
                System.err.println("Invalid follower ID ignored: " + s);
            }
        }
        return ids;
    }

    private String joinFollowerIds(Set<Long> followerIds) {
        if (followerIds.isEmpty()) {
            return null;
        }
        return followerIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    /**
     * Add a follower to a user
     * @param followerId The ID of the user who is following
     * @param userToFollowId The ID of the user to be followed
     * @return The updated user with new follower
     */
    public Utilisateur followUser(Long followerId, Long userToFollowId) {
        if (followerId.equals(userToFollowId)) {
            throw new RuntimeException("Cannot follow yourself");
        }

        Utilisateur userToFollow = utilisateurRepository.findById(userToFollowId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Set<Long> followerIds = parseFollowerIds(userToFollow.getFollowers());
        if (!followerIds.contains(followerId)) {
            followerIds.add(followerId);
            userToFollow.setFollowers(joinFollowerIds(followerIds));
            utilisateurRepository.save(userToFollow);

            // Send real-time notification
            notificationService.createFollowNotification(userToFollowId, followerId);
        }

        return userToFollow;
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

        Set<Long> followerIds = parseFollowerIds(userToUnfollow.getFollowers());
        if (followerIds.remove(followerId)) {
            userToUnfollow.setFollowers(joinFollowerIds(followerIds));
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

        Set<Long> followerIds = parseFollowerIds(user.getFollowers());

        if (followerIds.isEmpty()) {
            return List.of();
        }

        return followerIds.stream()
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

        return parseFollowerIds(user.getFollowers()).contains(followerId);
    }

    /**
     * Get the count of followers for a user
     * @param userId The ID of the user
     * @return Number of followers
     */
    public int getFollowersCount(Long userId) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return parseFollowerIds(user.getFollowers()).size();
    }
}
