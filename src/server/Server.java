package server;

import common.MessageListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Server {
    final ServerSocket serverSocket;
    private MessageListener uiListener;
    private final Map<Long, ClientThread> threads;
    private final AtomicLong idCounter = new AtomicLong(0);

    Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        threads = new HashMap<>();
    }

    void start() {
        new Thread(() -> {
            try {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    long id = idCounter.getAndIncrement();
                    ClientThread thread = new ClientThread(clientSocket, id, Server.this::onMessage, Server.this::onServerThreadClose);
                    threads.put(id, thread);
                    thread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    void sendMessage(String message) {
        threads.forEach((id, thread) -> thread.sendMessage("server", message));
    }

    void addListener(MessageListener listener) {
        uiListener = listener;
    }

    private void notifyListener(String message) {
        if (uiListener != null) uiListener.onMessageReceived(message);
    }

    private void onMessage(long id, String message) {
        String from = threads.get(id).getUsername();
        notifyListener(from + "(" + id + "): " + message);
        threads.values().stream()
                .filter(clientThread -> clientThread.getId() != id)
                .forEach(t -> t.sendMessage(from, message));
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
