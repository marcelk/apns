package nl.meandi.apns;

@SuppressWarnings("unused")
public enum NotificationPriority {
    TIME(10), POWER(5);

    private final int code;

    NotificationPriority(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
