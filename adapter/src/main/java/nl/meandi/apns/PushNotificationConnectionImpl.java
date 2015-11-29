package nl.meandi.apns;

import java.io.IOException;
import java.time.Instant;

public class PushNotificationConnectionImpl implements PushNotificationConnection {

    private ManagedConnectionImpl managedConnection;

    public PushNotificationConnectionImpl(ManagedConnectionImpl managedConnection) {
        this.managedConnection = managedConnection;
    }

    @Override
    public void sendNotification(byte[] deviceToken,
                                 byte[] payload,
                                 int notificationIdentifier,
                                 Instant expirationDate,
                                 NotificationPriority priority) throws IOException {
        managedConnection.sendNotification(deviceToken, payload, notificationIdentifier, expirationDate, priority);
    }

    public void setManagedConnection(ManagedConnectionImpl managedConnection) {
        this.managedConnection = managedConnection;
    }

    public ManagedConnectionImpl getManagedConnection() {
        return managedConnection;
    }

    @Override
    public void close() throws Exception {
        managedConnection.closeConnectionHandle(this);
    }
}
