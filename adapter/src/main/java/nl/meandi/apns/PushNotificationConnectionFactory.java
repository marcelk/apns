package nl.meandi.apns;

import javax.resource.Referenceable;
import java.io.Serializable;

public interface PushNotificationConnectionFactory extends Serializable, Referenceable {
    PushNotificationConnection getConnection();
}
