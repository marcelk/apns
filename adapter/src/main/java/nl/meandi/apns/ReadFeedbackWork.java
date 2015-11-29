package nl.meandi.apns;

import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.work.Work;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.util.List;

class ReadFeedbackWork implements Work {

    private static final int DEVICE_TOKEN_LENGTH = 32;

    private final Socket socket;

    private final List<MessageEndpoint> endpoints;

    private boolean stopping;

    public ReadFeedbackWork(Socket socket, List<MessageEndpoint> endpoints) {
        this.socket = socket;
        this.endpoints = endpoints;
    }

    @Override
    public void run() {
        try (DataInputStream inputStream = new DataInputStream(socket.getInputStream())) {
            boolean ready;
            do {
                ready = readFeedbackTuples(inputStream);
            } while (!ready && !stopping);
        } catch (IOException e) {
            if (!stopping) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean readFeedbackTuples(DataInputStream inputStream) throws IOException {
        int time;
        try {
            time = inputStream.readInt();
        } catch (EOFException e) {
            return true; // all tuples have been read
        }

        long unsignedTime = Integer.toUnsignedLong(time);
        Instant timestamp = Instant.ofEpochSecond(unsignedTime);

        int deviceTokenLength = inputStream.readUnsignedShort();
        if (deviceTokenLength != DEVICE_TOKEN_LENGTH) {
            throw new RuntimeException("Unexpected device token length");
        }

        byte[] deviceToken = new byte[DEVICE_TOKEN_LENGTH];
        inputStream.readFully(deviceToken);

        for (MessageEndpoint endpoint: endpoints) {
            ApnsListener apnsListener = (ApnsListener) endpoint;
            apnsListener.handleDeviceRemoval(timestamp, deviceToken);
        }

        return false; // there may be more tuples to read
    }

    @Override
    public void release() {
        stopping = true;
        try {
            socket.close(); // close the socket so that the read operation unblocks
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
