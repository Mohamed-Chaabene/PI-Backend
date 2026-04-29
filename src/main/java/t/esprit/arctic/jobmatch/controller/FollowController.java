package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.security.JwtService;
import t.esprit.arctic.jobmatch.service.FollowService;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final JwtService jwtService;

    /**
     * Follow a user
     * @param userToFollowId The ID of the user to follow
     * @param token JWT token from Authorization header
     * @return The updated user with new follower
     */
    @PostMapping("/{userToFollowId}/follow")
    public ResponseEntity<Utilisateur> followUser(
            @PathVariable Long userToFollowId,
            @RequestHeader("Authorization") String token) {
        Long followerId = extractUserIdFromToken(token);
        Utilisateur result = followService.followUser(followerId, userToFollowId);
        return ResponseEntity.ok(result);
    }

    /**
     * Unfollow a user
     * @param userToUnfollowId The ID of the user to unfollow
     * @param token JWT token from Authorization header
     * @return The updated user with follower removed
     */
    @PostMapping("/{userToUnfollowId}/unfollow")
    public ResponseEntity<Utilisateur> unfollowUser(
            @PathVariable Long userToUnfollowId,
            @RequestHeader("Authorization") String token) {
        Long followerId = extractUserIdFromToken(token);
        Utilisateur result = followService.unfollowUser(followerId, userToUnfollowId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get all followers of a user
     * @param userId The ID of the user
     * @return List of users who follow this user
     */
    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<Utilisateur>> getFollowers(@PathVariable Long userId) {
        List<Utilisateur> followers = followService.getFollowers(userId);
        return ResponseEntity.ok(followers);
    }

    /**
     * Check if current user is following a specific user
     * @param userIdToCheck The ID of the user to check if being followed
     * @param token JWT token from Authorization header
     * @return Map with boolean indicating if following
     */
    @GetMapping("/{userIdToCheck}/is-following")
    public ResponseEntity<Map<String, Object>> isFollowing(
            @PathVariable Long userIdToCheck,
            @RequestHeader("Authorization") String token) {
        Long followerId = extractUserIdFromToken(token);
        boolean isFollowing = followService.isFollowing(followerId, userIdToCheck);
        
        Map<String, Object> response = new HashMap<>();
        response.put("isFollowing", isFollowing);
        response.put("userId", userIdToCheck);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get the count of followers for a user
     * @param userId The ID of the user
     * @return Map with follower count
     */
    @GetMapping("/{userId}/followers-count")
    public ResponseEntity<Map<String, Object>> getFollowersCount(@PathVariable Long userId) {
        int count = followService.getFollowersCount(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("followersCount", count);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Extract user ID from JWT token
     */
    private Long extractUserIdFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token format");
        }
        String bearerToken = token.substring(7);
        return jwtService.extractId(bearerToken);
    }
}
