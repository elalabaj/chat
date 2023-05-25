package common;

@FunctionalInterface
public interface MessageListener {
    public void onMessageReceived(int group, String message);
}
