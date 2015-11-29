package nl.meandi.apns;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Set;

@ConnectionDefinition(connectionFactory=PushNotificationConnectionFactory.class,
        connectionFactoryImpl=PushNotificationConnectionFactoryImpl.class,
        connection=PushNotificationConnection.class,
        connectionImpl=PushNotificationConnectionImpl.class)
public class ManagedConnectionFactoryImpl implements ManagedConnectionFactory, ResourceAdapterAssociation {

    private PrintWriter logWriter;

    private ApnsResourceAdapter resourceAdapter;

    @ConfigProperty
    private String certificateFileName = ApnsResourceAdapter.DEFAULT_CERTIFICATE_FILE_NAME;

    @ConfigProperty(confidential = true)
    private String certificateFilePassword;

    @Override
    public Object createConnectionFactory(ConnectionManager connectionManager) throws ResourceException {
        return new PushNotificationConnectionFactoryImpl(this, connectionManager);
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        return createConnectionFactory(new ConnectionManagerImpl());
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        return new ManagedConnectionImpl(logWriter, this);
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        if (connectionSet.size() > 0) {
            return (ManagedConnection) connectionSet.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public void setLogWriter(PrintWriter logWriter) throws ResourceException {
        this.logWriter = logWriter;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    @Override
    public ApnsResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
        this.resourceAdapter = (ApnsResourceAdapter) ra;
    }

    public String getCertificateFileName() {
        return certificateFileName;
    }

    @SuppressWarnings("unused")
    public void setCertificateFileName(String certificateFileName) {
        this.certificateFileName = certificateFileName;
    }

    public String getCertificateFilePassword() {
        return certificateFilePassword;
    }

    @SuppressWarnings("unused")
    public void setCertificateFilePassword(String certificateFilePassword) {
        this.certificateFilePassword = certificateFilePassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManagedConnectionFactoryImpl that = (ManagedConnectionFactoryImpl) o;
        return Objects.equals(certificateFileName, that.certificateFileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certificateFileName);
    }
}