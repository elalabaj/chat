package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    final Socket clientSocket;
    final PrintWriter writer;
    final BufferedReader reader;
    final Scanner scanner;
    Client () throws IOException {
        clientSocket = new Socket("127.0.0.1", 5000);
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
                System.out.println("Server disconnected");
                writer.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiver.start();
    }
    public static void main(String[] args) {
        try {
            new Client();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
