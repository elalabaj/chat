package server;

import common.MessageListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Server {
    final ServerSocket serverSocket;
    private MessageListener uiListener;
    private final Map<Long, ClientThread> threads;
    private final List<List<Long>> groups;
    private final AtomicLong idCounter = new AtomicLong(0);

    Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        threads = new HashMap<>();
        groups = new ArrayList<>();
        groups.add(new ArrayList<>());
    }

    public Map<Long, ClientThread> getThreads() {
        return threads;
    }

    void start() {
        new Thread(() -> {
            try {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    long id = idCounter.getAndIncrement();
                    ClientThread thread = new ClientThread(clientSocket, id, Server.this::onMessage, Server.this::onServerThreadClose);
                    threads.put(id, thread);
                    groups.get(0).add(id);
                    thread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    void sendMessage(int group, String message) {
        groups.get(group).forEach(id -> threads.get(id).sendMessage("server", group, message));
    }

    void addListener(MessageListener listener) {
        uiListener = listener;
    }

    private void notifyListener(int group, String message) {
        if (uiListener != null) uiListener.onMessageReceived(group, message);
    }

    public void addGroup(String name, Long[] userIds) {
        groups.add(new ArrayList<>(List.of(userIds)));
        sendMessage(groups.size() - 1, name);
    }

    private void onMessage(long id, int group, String message) {
        String from = threads.get(id).getUsername();
        notifyListener(group, from + "(" + id + "): " + message);
        groups.get(group).stream()
                .filter(clientId -> clientId != id)
                .forEach(clientId -> threads.get(clientId).sendMessage(from, group, message));
    }

    private void onServerThreadClose(long id) {
        threads.remove(id);
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(5000);
            WindowServer windowServer = new WindowServer(server);
            server.addListener(windowServer);
            server.start();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
