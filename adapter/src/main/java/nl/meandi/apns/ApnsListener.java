package nl.meandi.apns;

import java.time.Instant;

public interface ApnsListener {
    void handleDeviceRemoval(Instant timestamp, byte[] deviceId);
}
