package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.LongConsumer;

public class ClientThread extends Thread {
    private final Socket clientSocket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final long id;
    private String username;
    private final MessageConsumer messageConsumer;
    private final LongConsumer closeConsumer;
    private final ExecutorService executorService;
    public ClientThread(Socket clientSocket, long id, MessageConsumer messageConsumer, LongConsumer closeConsumer) throws IOException {
        this.clientSocket = clientSocket;
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        writer = new PrintWriter(clientSocket.getOutputStream());
        this.id = id;
        this.messageConsumer = messageConsumer;
        this.closeConsumer = closeConsumer;
        executorService = Executors.newSingleThreadExecutor();
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void close() throws IOException {
        clientSocket.close();
        reader.close();
        writer.close();
        executorService.close();
        closeConsumer.accept(id);
    }

    public void sendMessage(String from, int group, String message) {
        executorService.execute(() -> {
            try {
                writer.println(from + "|" + group + "|" + message);
                writer.flush();
            } catch (Throwable e) {
                System.out.println(e.getMessage());
            }
        });
    }

    @Override
    public void run() {
        try {
            String message = reader.readLine();
            while (message != null) {
                String[] split = message.split("\\|");
                int group = Integer.parseInt(split[0]);
                String body = split[1];
                if (username == null) {
                    username = body;
                    messageConsumer.accept(id, group, body + " joined");
                } else {
                    messageConsumer.accept(id, group, body);
                }
                message = reader.readLine();
            }
            System.out.println("closes");
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
