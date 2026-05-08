package server.network;

import server.dao.*;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            String clientMessage;

            while ((clientMessage = reader.readLine()) != null) {
                System.out.println("Received from client: " + clientMessage);
                String response = handleRequest(clientMessage);
                writer.println(response);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
        } finally {
            closeConnection();
        }
    }

    private String handleRequest(String message) {
        System.out.println("=== Xử lý request: " + message + " ===");

        // XỬ LÝ LOGIN
        if (message.startsWith("LOGIN")) {
            // Format: "LOGIN username password"
            String[] parts = message.split(" ");
            if (parts.length == 3) {
                String username = parts[1];
                String password = parts[2];

                System.out.println("Đăng nhập - Username: " + username + ", Password: " + password);

                // GỌI DATABASE ĐỂ KIỂM TRA
                boolean success = UserDAO.login(username, password);

                if (success) {
                    System.out.println("✓ Đăng nhập thành công: " + username);
                    return "LOGIN_SUCCESS";
                } else {
                    System.out.println("✗ Đăng nhập thất bại: " + username);
                    return "LOGIN_FAILED";
                }
            } else {
                return "ERROR: Invalid LOGIN format";
            }
        }

        // XỬ LÝ REGISTER
        else if (message.startsWith("REGISTER")) {

            String[] data = message.split("\\|");

            if (data.length == 5) {

                String command = data[0];
                String fullname = data[1];
                String username = data[2];
                String email = data[3];
                String password = data[4];

                System.out.println("Đăng ký - Username: " + username);

                boolean success = UserDAO.register(
                        fullname,
                        username,
                        email,
                        password
                );

                if (success) {
                    System.out.println("✓ Đăng ký thành công");
                    return "REGISTER_SUCCESS";
                } else {
                    System.out.println("✗ Đăng ký thất bại");
                    return "REGISTER_FAILED";
                }

            } else {
                return "ERROR: Invalid REGISTER format";
            }
        }

        // XỬ LÝ GET_AUCTIONS (giữ nguyên)
        else if (message.startsWith("GET_AUCTIONS")) {
            return "AUCTION_LIST_DATA";
        }

        return "ERROR: Unknown Command";
    }

    private void closeConnection() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}