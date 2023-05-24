package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.LongConsumer;

public class ClientThread extends Thread {
    private final Socket clientSocket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final long id;
    private String username;
    private final BiConsumer<Long, String> messageConsumer;
    private final LongConsumer closeConsumer;
    private final ExecutorService executorService;
    public ClientThread(Socket clientSocket, long id, BiConsumer<Long, String> messageConsumer, LongConsumer closeConsumer) throws IOException {
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

    public void sendMessage(String from, String message) {
        System.out.println("ServerThread.sendMessage " + message + " " + currentThread().threadId());
        executorService.execute(() -> {
            try {
                writer.println(from + ": " + message);
                writer.flush();
            } catch (Throwable e) {
                System.out.println(e.getMessage());
            }
        });
    }

    @Override
    public void run() {
        System.out.println("ServerThread.run() " + currentThread().threadId());
        try {
            String message = reader.readLine();
            while (message != null) {
                if (username == null) {
                    username = message;
                } else {
                    messageConsumer.accept(id, message);
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
