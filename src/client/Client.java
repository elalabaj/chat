package client;

import common.MessageListener;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Client {
    private String username;
    private final Socket clientSocket;
    private final PrintWriter writer;
    private final BufferedReader reader;
    private MessageListener messageListener;
    private final Map<Integer, String> groupNames;

    Client (String host, int port) throws IOException {
        clientSocket = new Socket(host, port);
        writer = new PrintWriter(clientSocket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        groupNames = new HashMap<>();
        groupNames.put(0, "main");

        start();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    void close() throws IOException {
        sendMessage(0, username + " leaved");
        clientSocket.close();
        writer.close();
        reader.close();
    }

    void start() {
        Thread receiver = new Thread(() -> {
            try {
                String message = reader.readLine();
                while (message != null) {
                    onMessage(message);
                    message = reader.readLine();
                }
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiver.start();
    }

    void onMessage(String message) {
        String[] split = message.split("\\|");
        String from = split[0];
        int groupId = Integer.parseInt(split[1]);
        String body = split[2];
        if (!groupNames.containsKey(groupId)) {
            groupNames.put(groupId, body);
            notifyListener(groupId, body);
        } else {
            notifyListener(groupId, from + ": " + body);
        }
    }

    void sendMessage(int group, String message) {
        try (ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()) {
            scheduledExecutorService.execute(() -> {
                writer.println(group + "|" + message);
                writer.flush();
            });
        }
    }

    void addListener(MessageListener listener) {
        messageListener = listener;
    }

    private void notifyListener(int group, String message) {
        if (messageListener != null) messageListener.onMessageReceived(group, message);
    }

    public static void main(String[] args) {
        try {
            Client client = new Client("127.0.0.1", 5000);
            WindowClient windowClient = new WindowClient(client);
            String username = JOptionPane.showInputDialog(windowClient, "Enter username:", "Username", JOptionPane.QUESTION_MESSAGE);
            if (username == null) System.exit(-1);
            windowClient.setTitle(username);
            client.sendMessage(0, username);
            client.addListener(windowClient);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
