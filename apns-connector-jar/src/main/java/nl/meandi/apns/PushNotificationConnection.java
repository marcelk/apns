package nl.meandi.apns;

import java.io.IOException;
import java.time.Instant;

public interface PushNotificationConnection extends AutoCloseable {
    @SuppressWarnings("unused")
    void sendNotification(byte[] deviceToken,
                          byte[] payload,
                          int notificationIdentifier,
                          Instant expirationDate,
                          NotificationPriority priority) throws IOException;
}
