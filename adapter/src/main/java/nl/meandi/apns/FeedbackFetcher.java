package nl.meandi.apns;

import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

class FeedbackFetcher extends TimerTask {

    private static final Logger LOG = Logger.getLogger(FeedbackFetcher.class.getName());

    private final ApnsActivationSpec activationSpec;

    private final Timer timer;

    private final List<MessageEndpointFactory> endpointFactories = new LinkedList<>();

    private ReadFeedbackWork readFeedbackWork;

    public FeedbackFetcher(ApnsResourceAdapter resourceAdapter, ApnsActivationSpec activationSpec) {
        this.activationSpec = activationSpec;

        try {
            timer = resourceAdapter.getBootstrapContext().createTimer();
        } catch (UnavailableException e) {
            throw new RuntimeException(e);
        }
        timer.schedule(this, 0L, 24 * 60 * 60 * 1000L);
        LOG.info("Started");
    }

    public synchronized void addEndpointFactory(MessageEndpointFactory endpointFactory) {
        endpointFactories.add(endpointFactory);
    }

    public synchronized void removeEndpointFactory(MessageEndpointFactory endpointFactory) {
        endpointFactories.remove(endpointFactory);
    }

    public synchronized void destroy() {
        timer.cancel();
        if (readFeedbackWork != null) {
            readFeedbackWork.release();
            while (readFeedbackWork != null) {
                try {
                    wait();
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        LOG.info("Stopped");
    }

    public synchronized void run() {
        LOG.info("Started fetching feedback");

        List<MessageEndpoint> endpoints = new LinkedList<>();
        for (MessageEndpointFactory endpointFactory: endpointFactories) {
            MessageEndpoint endpoint;
            try {
                endpoint = endpointFactory.createEndpoint(null);
            } catch (UnavailableException e) {
                throw new RuntimeException(e);
            }
            endpoints.add(endpoint);
        }

        Socket socket = SocketFactory.createSocket(
                "feedback",
                2196,
                activationSpec.getCertificateFileName(),
                activationSpec.getCertificateFilePassword());

        readFeedbackWork = new ReadFeedbackWork(socket, endpoints);

        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        endpoints.forEach(MessageEndpoint::release);
        LOG.info("Ready fetching feedback");
    }

    public List<MessageEndpointFactory> getEndpointFactories() {
        return endpointFactories;
    }
}
