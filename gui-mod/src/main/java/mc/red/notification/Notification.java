package mc.red.notification;

class Notification {
    final String text;
    final boolean enabled;
    final long startTime;
    final long duration;

    Notification(String text, boolean enabled, long duration) {
        this.text = text;
        this.enabled = enabled;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    boolean isExpired() {
        return System.currentTimeMillis() - startTime >= duration;
    }
}
