package nl.meandi;

import nl.meandi.apns.ApnsListener;
import org.jboss.ejb3.annotation.ResourceAdapter;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.xml.bind.DatatypeConverter;
import java.time.Instant;
import java.util.logging.Logger;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(
                propertyName = "certificateFileName",
                propertyValue = "${env.APNS_CERTIFICATE_FILE_NAME}"),
        @ActivationConfigProperty(
                propertyName = "certificateFilePassword",
                propertyValue = "${env.APNS_CERTIFICATE_FILE_PASSWORD}")
})
@ResourceAdapter("apns")
@SuppressWarnings("unused")
public class MessageDrivenBean implements ApnsListener {

    private static final Logger LOG = Logger.getLogger(MessageDrivenBean.class.getName());

    @Override
    public void handleDeviceRemoval(Instant timestamp, byte[] deviceId) {
        LOG.info("Device with id " + DatatypeConverter.printHexBinary(deviceId) + " has been removed at " + timestamp);
    }
}
