package client;

import server.MessageListener;
import server.WindowServer;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Client {
    private final Socket clientSocket;
    private final PrintWriter writer;
    private final BufferedReader reader;
    private final Scanner scanner;
    private MessageListener messageListener;

    Client (String host, int port) throws IOException {
        clientSocket = new Socket(host, port);
        writer = new PrintWriter(clientSocket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        scanner = new Scanner(System.in);

        Thread receiver = new Thread(() -> {
            try {
                String message = reader.readLine();
                while (message != null) {
                    notifyListener(message);
                    message = reader.readLine();
                }
                writer.close();
                clientSocket.close();
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
            Client client = new Client("127.0.0.1", 5000);
            WindowClient windowClient = new WindowClient(client);
            client.addListener(windowClient);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
