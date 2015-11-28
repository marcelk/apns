package nl.meandi.apns;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;

public class PushNotificationConnectionFactoryImpl implements PushNotificationConnectionFactory {

    private final ManagedConnectionFactoryImpl managedConnectionFactory;

    private final ConnectionManager connectionManager;

    private Reference reference;

    public PushNotificationConnectionFactoryImpl(ManagedConnectionFactoryImpl managedConnectionFactory, ConnectionManager connectionManager) {
        this.managedConnectionFactory = managedConnectionFactory;
        this.connectionManager = connectionManager;
    }

    @Override
    public PushNotificationConnection getConnection() {
        try {
            return (PushNotificationConnection) connectionManager.allocateConnection(managedConnectionFactory, null);
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setReference(Reference ref) {
        reference = ref;
    }

    @Override
    public Reference getReference() throws NamingException {
        return reference;
    }
}
