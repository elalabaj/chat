package server;

@FunctionalInterface
public interface MessageConsumer {
    public void accept(long id, int group, String message);
}
