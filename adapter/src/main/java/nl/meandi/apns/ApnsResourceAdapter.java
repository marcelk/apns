package nl.meandi.apns;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Connector
@SuppressWarnings("unused")
public class ApnsResourceAdapter implements ResourceAdapter {

    public static final String DEFAULT_CERTIFICATE_FILE_NAME = "/etc/apns/certificate.p12";

    private static final Logger LOG = Logger.getLogger(ApnsResourceAdapter.class.getName());

    private BootstrapContext bootstrapContext;

    private final Map<ApnsActivationSpec, FeedbackFetcher> feedbackFetchers = new HashMap<>();

    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        LOG.info("Started");
        this.bootstrapContext = ctx;
    }

    @Override
    public void stop() {
        LOG.info("Stopped");
    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException {
        ApnsActivationSpec apnsActivationSpec = (ApnsActivationSpec) spec;

        feedbackFetchers
                .computeIfAbsent(apnsActivationSpec, (activationSpec) -> new FeedbackFetcher(this, activationSpec))
                .addEndpointFactory(endpointFactory);
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        ApnsActivationSpec apnsActivationSpec = (ApnsActivationSpec) spec;
        FeedbackFetcher feedbackFetcher = feedbackFetchers.get(apnsActivationSpec);
        feedbackFetcher.removeEndpointFactory(endpointFactory);
        if (feedbackFetcher.getEndpointFactories().isEmpty()) {
            feedbackFetcher.destroy();
            feedbackFetchers.remove(spec, feedbackFetcher);
        }
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        return null;
    }

    public BootstrapContext getBootstrapContext() {
        return bootstrapContext;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
