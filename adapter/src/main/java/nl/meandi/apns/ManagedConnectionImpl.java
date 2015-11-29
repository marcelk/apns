package nl.meandi.apns;

import javax.net.ssl.SSLSocket;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ManagedConnectionImpl implements ManagedConnection, WorkListener {

    private PrintWriter logWriter;

    private final ManagedConnectionFactoryImpl managedConnectionFactory;

    private final Set<ConnectionEventListener> listeners;

    private final Set<PushNotificationConnectionImpl> connectionHandles;

    private Work work;

    private Socket socket;

    private DataOutputStream outputStream;

    public ManagedConnectionImpl(PrintWriter logWriter, ManagedConnectionFactoryImpl managedConnectionFactory) {
        this.logWriter = logWriter;
        this.managedConnectionFactory = managedConnectionFactory;
        listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());
        connectionHandles = Collections.newSetFromMap(new ConcurrentHashMap<>());

        BootstrapContext bootstrapContext = managedConnectionFactory.getResourceAdapter().getBootstrapContext();

        socket = createSocket();
        try {
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException();
        }
        work = new ReadErrorResponseWork(socket, logWriter, this);
        try {
            bootstrapContext.getWorkManager().scheduleWork(work, Long.MAX_VALUE, null, this);
        } catch (WorkException e) {
            e.printStackTrace(logWriter);
        }
        logWriter.println("Started connection with Apple server");
    }

    private SSLSocket createSocket() {
        String hostPrefix = "gateway";
        int port = 2195;
        String certificateFileName = managedConnectionFactory.getCertificateFileName();
        String certificateFilePassword = managedConnectionFactory.getCertificateFilePassword();
        return SocketFactory.createSocket(hostPrefix, port, certificateFileName, certificateFilePassword);
    }

    @Override
    public synchronized void destroy() throws ResourceException {
        work.release();
        while (work != null) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        logWriter.println("Stopped connection with Apple server");
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        PushNotificationConnectionImpl connectionHandle = new PushNotificationConnectionImpl(this);
        connectionHandles.add(connectionHandle);
        return connectionHandle;
    }

    public void closeConnectionHandle(Object connectionHandle) {
        PushNotificationConnectionImpl pushNotificationConnection = (PushNotificationConnectionImpl) connectionHandle;
        connectionHandles.remove(pushNotificationConnection);
        listeners.forEach(listener -> {
            ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
            event.setConnectionHandle(pushNotificationConnection);
            listener.connectionClosed(event);
        });
    }

    public void handleConnectionRelatedError() {
        for (ConnectionEventListener listener: listeners) {
            ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
            listener.connectionErrorOccurred(event);
        }
    }

    @Override
    public void cleanup() throws ResourceException {
        for (PushNotificationConnectionImpl pushNotificationConnection: connectionHandles) {
            pushNotificationConnection.setManagedConnection(null);
        }
        connectionHandles.clear();
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        PushNotificationConnectionImpl pushNotificationConnection = (PushNotificationConnectionImpl) connection;
        ManagedConnectionImpl managedConnection = pushNotificationConnection.getManagedConnection();
        managedConnection.connectionHandles.remove(pushNotificationConnection);
        pushNotificationConnection.setManagedConnection(this);
        connectionHandles.add(pushNotificationConnection);
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        listeners.remove(listener);
    }

    public void sendNotification(byte[] deviceToken,
                                 byte[] payload,
                                 int notificationIdentifier,
                                 Instant expirationDate,
                                 NotificationPriority priority) throws IOException {
        try {
            outputStream.writeByte(2); //command
            byte[] frameBytes = createFrame(deviceToken, payload, notificationIdentifier, expirationDate, priority);
            outputStream.writeInt(frameBytes.length);
            outputStream.write(frameBytes);
        } catch (IOException e) {
            handleConnectionRelatedError();
            throw e;
        }
    }

    private byte[] createFrame(byte[] deviceToken,
                               byte[] payload,
                               int notificationIdentifier,
                               Instant expirationDate,
                               NotificationPriority priority) throws IOException {
        byte[] frameBytes;

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(2500);
             DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {

            // Device token
            dataOutputStream.writeByte(1);
            dataOutputStream.writeShort(32);
            dataOutputStream.write(deviceToken);

            // Payload
            dataOutputStream.writeByte(2);
            dataOutputStream.writeShort(payload.length);
            dataOutputStream.write(payload);

            // Notification
            dataOutputStream.writeByte(3);
            dataOutputStream.writeShort(4);
            dataOutputStream.writeInt(notificationIdentifier);

            // Expiration date
            dataOutputStream.writeByte(4);
            dataOutputStream.writeShort(4);
            dataOutputStream.writeInt((int) (expirationDate.toEpochMilli() / 1000));

            // Notification
            dataOutputStream.writeByte(5);
            dataOutputStream.writeShort(1);
            dataOutputStream.writeByte(priority.getCode());

            frameBytes = byteArrayOutputStream.toByteArray();
        }

        return frameBytes;
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        throw new NotSupportedException();
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        throw new NotSupportedException();
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return new ManagedConnectionMetaData() {

            public String getEISProductName() throws ResourceException {
                return null;
            }

            public String getEISProductVersion() throws ResourceException {
                return null;
            }

            public int getMaxConnections() throws ResourceException {
                return 2;
            }

            public String getUserName() throws ResourceException {
                return null;
            }
        };
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
    public void workAccepted(WorkEvent e) {
    }

    @Override
    public synchronized void workRejected(WorkEvent e) {
        handleConnectionRelatedError();
        work = null;
        socket = null;
        notifyAll();
    }

    @Override
    public void workStarted(WorkEvent e) {
    }

    @Override
    public synchronized void workCompleted(WorkEvent e) {
        work = null;
        notifyAll();
    }
}
