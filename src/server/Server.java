package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    final ServerSocket serverSocket;
    final Socket clientSocket;
    final BufferedReader reader;
    final PrintWriter writer;
    final Scanner scanner;

    Server() throws IOException {
        serverSocket = new ServerSocket(5000);
        clientSocket = serverSocket.accept();
        writer = new PrintWriter(clientSocket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        scanner = new Scanner(System.in);

        Thread sender = new Thread(() -> {
            String message;
            while (true) {
                message = scanner.nextLine();
                writer.println(message);
                writer.flush();
            }
        });
        sender.start();

        Thread receiver = new Thread(() -> {
            try {
                String message = reader.readLine();
                while (message != null) {
                    System.out.println(message);
                    message = reader.readLine();
                }
                System.out.println("Client disconnected");
                writer.close();
                clientSocket.close();
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiver.start();
    }
    public static void main(String[] args) {
        try {
            new Server();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
