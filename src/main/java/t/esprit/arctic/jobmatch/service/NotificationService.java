package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.pusher.rest.Pusher;
import t.esprit.arctic.jobmatch.entity.Notification;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.repository.NotificationRepository;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final Pusher pusher;

    /**
     * Create a follow notification and send via Pusher
     */
    public Notification createFollowNotification(Long userId, Long senderId) {
        // Get sender details for message
        Utilisateur sender = utilisateurRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // Create notification
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setSenderId(senderId);
        notification.setType("follow");
        notification.setMessage(sender.getNom() + " started following you");
        notification.setIsRead(false);

        // Save to database
        Notification savedNotification = notificationRepository.save(notification);

        // Send real-time notification via Pusher
        sendPusherNotification(userId, savedNotification);

        return savedNotification;
    }

    /**
     * Send notification via Pusher Channels
     */
    private void sendPusherNotification(Long userId, Notification notification) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("id", notification.getId());
            data.put("type", notification.getType());
            data.put("message", notification.getMessage());
            data.put("senderId", notification.getSenderId());
            data.put("createdAt", notification.getCreatedAt().toString());

            // Send to private channel for specific user
            String channelName = "private-user-" + userId;
            pusher.trigger(channelName, "new-notification", data);

            System.out.println("✅ Pusher notification sent to " + channelName);
        } catch (Exception e) {
            System.err.println("❌ Error sending Pusher notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get all notifications for a user
     */
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get unread notifications for a user
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Get count of unread notifications
     */
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Mark notification as read
     */
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        unreadNotifications.forEach(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    /**
     * Delete all notifications for a user
     */
    public void deleteAllNotifications(Long userId) {
        List<Notification> allNotifications = getUserNotifications(userId);
        notificationRepository.deleteAll(allNotifications);
        System.out.println("✅ All notifications deleted for user: " + userId);
    }
}
