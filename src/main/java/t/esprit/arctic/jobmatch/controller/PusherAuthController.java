package t.esprit.arctic.jobmatch.controller;

import com.pusher.rest.Pusher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.security.JwtService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pusher")
@RequiredArgsConstructor
public class PusherAuthController {

    private final Pusher pusher;
    private final JwtService jwtService;

    /**
     * Authenticate private channel subscription
     * Called by frontend before subscribing to private channels
     */
    @PostMapping("/auth")
    public String authenticateChannel(
            @RequestHeader("Authorization") String token,
            @RequestParam String socket_id,
            @RequestParam String channel_name) {
        
        try {
            // Verify JWT and extract user ID
            Long userId = extractUserIdFromToken(token);
            
            // Verify user is authorized for this channel
            String expectedChannelName = "private-user-" + userId;
            if (!channel_name.equals(expectedChannelName)) {
                return "{\"error\": \"Unauthorized channel access\"}";
            }

            System.out.println("✅ Authorizing channel: " + channel_name + " for user: " + userId);

            // Generate Pusher authentication token - returns JSON string
            String authResponse = pusher.authenticate(socket_id, channel_name);
            
            System.out.println("✅ Pusher auth response: " + authResponse);
            
            // Return the auth response directly (it's already a JSON string)
            return authResponse;
        } catch (Exception e) {
            System.err.println("❌ Channel authentication error: " + e.getMessage());
            e.printStackTrace();
            return "{\"error\": \"Authentication failed\"}";
        }
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
