package nl.meandi;

import nl.meandi.apns.NotificationPriority;
import nl.meandi.apns.PushNotificationConnection;
import nl.meandi.apns.PushNotificationConnectionFactory;

import javax.annotation.Resource;
import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.validation.constraints.Pattern;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

@Model
public class MessengerBackingBean {

    @Resource(lookup = "java:/eis/apns-connector")
    private PushNotificationConnectionFactory connectionFactory;

    @Pattern(regexp = "(\\p{XDigit}| )*")
    private String deviceToken;

    private String message;

    public void sendMessage() {
        try (PushNotificationConnection conn = connectionFactory.getConnection()) {
            byte[] deviceTokenBytes = DatatypeConverter.parseHexBinary(deviceToken.replaceAll(" ", ""));
            byte[] payload = createPayload(message);
            int notificationIdentifier = 123;
            Instant expirationDate = Instant.now().plus(Duration.ofHours(10));
            NotificationPriority priority = NotificationPriority.TIME;

            conn.sendNotification(deviceTokenBytes, payload, notificationIdentifier, expirationDate, priority);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage("Message has been sent");
        facesContext.addMessage(null, facesMessage);
    }

    private byte[] createPayload(String message) {
        JsonBuilderFactory factory = Json.createBuilderFactory(null);
        JsonObjectBuilder alertBuilder = factory.createObjectBuilder();
        alertBuilder.add("alert", message);

        JsonObjectBuilder apsBuilder = factory.createObjectBuilder();
        apsBuilder.add("aps", alertBuilder);

        String jsonString = apsBuilder.build().toString();
        return jsonString.getBytes(StandardCharsets.UTF_8);
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
