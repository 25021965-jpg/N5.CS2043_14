package server.network;

import server.dao.UserDAO;
import model.User;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

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

            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            writer = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

            String clientMessage;

            while ((clientMessage = reader.readLine()) != null) {

                System.out.println(
                        "Received from client: " + clientMessage
                );

                String response =
                        handleRequest(clientMessage);

                writer.println(response);
            }

        } catch (IOException e) {

            System.out.println(
                    "Client disconnected: "
                            + socket.getInetAddress()
            );

        } finally {

            closeConnection();
        }
    }

    private String handleRequest(String message) {

        System.out.println(
                "=== Xử lý request: " + message + " ==="
        );

        // LOGIN
        if (message.startsWith("LOGIN")) {

            String[] parts = message.split("\\|");

            if (parts.length == 3) {

                String username = parts[1];
                String password = parts[2];

                System.out.println(
                        "Login - Username: "
                                + username
                );

                boolean success =
                        UserDAO.login(username, password);

                if (success) {

                    System.out.println(
                            "Login success"
                    );

                    return "LOGIN_SUCCESS";

                } else {

                    System.out.println(
                            "Login failed"
                    );

                    return "LOGIN_FAILED";
                }

            } else {

                return "ERROR: Invalid LOGIN format";
            }
        }

        // REGISTER
        else if (message.startsWith("REGISTER")) {

            String[] data = message.split("\\|");

            if (data.length == 5) {

                String fullname = data[1];
                String username = data[2];
                String email = data[3];
                String password = data[4];

                System.out.println(
                        fullname + " | " +
                                username + " | " +
                                email
                );

                UserDAO userDAO = new UserDAO();

                // check email tồn tại
                if (userDAO.findByEmail(email) != null) {

                    System.out.println(
                            "Email already exists"
                    );

                    return "REGISTER_FAILED";
                }

                User user = new User();

                user.setId(
                        UUID.randomUUID().toString()
                );

                user.setFullname(fullname);
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(password);

                try {

                    userDAO.save(user);

                    System.out.println(
                            "Register success"
                    );

                    return "REGISTER_SUCCESS";

                } catch (Exception e) {

                    e.printStackTrace();

                    return "REGISTER_FAILED";
                }

            } else {

                return "ERROR: Invalid REGISTER format";
            }
        }

        // GET AUCTIONS
        else if (message.startsWith("GET_AUCTIONS")) {

            return "AUCTION_LIST_DATA";
        }

        return "ERROR: Unknown Command";
    }

    private void closeConnection() {

        try {

            if (socket != null) {
                socket.close();
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}