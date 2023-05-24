package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Server {
    final ServerSocket serverSocket;
    Socket clientSocket;
    BufferedReader reader;
    PrintWriter writer;
    private MessageListener messageListener;

    Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    void stop() throws IOException {
        writer.close();
        clientSocket.close();
        serverSocket.close();
    }

    void start() throws IOException {
        clientSocket = serverSocket.accept();
        writer = new PrintWriter(clientSocket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        Thread receiver = new Thread(() -> {
            try {
                String message = reader.readLine();
                while (message != null) {
                    notifyListener(message);
                    message = reader.readLine();
                }
                stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiver.start();
    }

    void sendMessage(String message) {
        try (ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()) {
            scheduledExecutorService.execute(() -> {
                writer.println(message);
                writer.flush();
            });
        }
    }

    void addListener(MessageListener listener) {
        messageListener = listener;
    }

    private void notifyListener(String message) {
        if (messageListener != null) messageListener.onMessageReceived(message);
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
