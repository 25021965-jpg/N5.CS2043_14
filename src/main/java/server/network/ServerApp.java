package server.network;

import server.dao.DatabaseService;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ServerApp {
    private static final int PORT = 9999;

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        System.out.println("--- Auction System Server Starting ---");

        // Khởi tạo database trước khi lắng nghe
        try {
            DatabaseService.initDatabase();
            System.out.println("✓ Database initialization check completed.");
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Could not initialize database!");
            System.err.println("Error: " + e.getMessage());
            return; // Dừng server nếu không thể kết nối database
        }

        // Bắt đầu lắng nghe kết nối từ client
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port: " + PORT);
            System.out.println("Waiting for clients to connect...");

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
            System.err.println("Error: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }
}