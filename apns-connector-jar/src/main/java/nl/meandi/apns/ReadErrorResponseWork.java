package nl.meandi.apns;

import javax.resource.spi.work.Work;
import javax.xml.bind.DatatypeConverter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

class ReadErrorResponseWork implements Work {

    private final Socket socket;

    private final PrintWriter logWriter;

    private final ManagedConnectionImpl managedConnection;

    private boolean stopping;

    public ReadErrorResponseWork(Socket socket, PrintWriter logWriter, ManagedConnectionImpl managedConnection) {
        this.socket = socket;
        this.logWriter = logWriter;
        this.managedConnection = managedConnection;
    }

    @Override
    public void run() {
        try (DataInputStream inputStream = new DataInputStream(socket.getInputStream())) {
            int command = inputStream.readUnsignedByte();
            if (command != 8) {
                throw new RuntimeException("Received packet with unsupported command (" + command + ")");
            }
            int status = inputStream.readUnsignedByte();
            String statusString = getStatusString(status);
            byte[] identifier = new byte[4];
            inputStream.readFully(identifier);
            logWriter.println("Received error response packet:");
            logWriter.println("- Status: " + statusString);
            logWriter.println("- Identifier: " + DatatypeConverter.printHexBinary(identifier));
        } catch (IOException e) {
            if (stopping) {
                return;
            }
            e.printStackTrace(logWriter);
        }

        managedConnection.handleConnectionRelatedError();
    }

    private String getStatusString(int status) {
        switch (status) {
            case 0:
                return "No errors encountered";
            case 1:
                return "Processing error";
            case 2:
                return "Missing device token";
            case 3:
                return "Missing topic";
            case 4:
                return "Missing payload";
            case 5:
                return "Invalid token size";
            case 6:
                return "Invalid topic size";
            case 7:
                return "Invalid payload size";
            case 8:
                return "Invalid token";
            case 10:
                return "Shutdown";
            case 255:
                return "None (unknown)";
            default:
                throw new RuntimeException("Invalid status: " + status);
        }
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
