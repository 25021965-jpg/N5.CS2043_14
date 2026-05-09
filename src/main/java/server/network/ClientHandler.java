package server.network;

import model.User;
import server.dao.UserDAO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import java.util.UUID;

public class ClientHandler implements Runnable {

    private Socket socket;

    private BufferedReader reader;

    private PrintWriter writer;

    public ClientHandler(
            Socket socket
    ) {

        this.socket = socket;
    }

    @Override
    public void run() {

        try {

            reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()
                            )
                    );

            writer =
                    new PrintWriter(
                            socket.getOutputStream(),
                            true
                    );

            String clientMessage;

            while (
                    (clientMessage =
                            reader.readLine()) != null
            ) {

                System.out.println(
                        "Received from client: "
                                + clientMessage
                );

                String response =
                        handleRequest(
                                clientMessage
                        );

                System.out.println(
                        "Response: "
                                + response
                );

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

    private String handleRequest(
            String message
    ) {

        System.out.println(
                "Handle request: "
                        + message
        );

        // LOGIN
        if (
                message.startsWith(
                        "LOGIN"
                )
        ) {

            String[] parts =
                    message.split("\\|");

            if (parts.length == 3) {

                String input =
                        parts[1].trim();

                String password =
                        parts[2].trim();

                System.out.println(
                        "Login: "
                                + input
                );

                boolean success =
                        UserDAO.login(
                                input,
                                password
                        );

                if (success) {

                    UserDAO userDAO =
                            new UserDAO();

                    User user =
                            userDAO.findByUsernameOrEmail(
                                    input
                            );

                    if (user == null) {

                        return "LOGIN_FAILED";
                    }

                    System.out.println(
                            "Login success"
                    );

                    return "LOGIN_SUCCESS|"
                            + user.getId() + "|"
                            + user.getFullname() + "|"
                            + user.getUsername() + "|"
                            + user.getEmail() + "|"
                            + (
                            user.getDob() == null
                                    ? ""
                                    : user.getDob()
                    );

                } else {

                    System.out.println(
                            "Login failed"
                    );

                    return "LOGIN_FAILED";
                }

            } else {

                return "ERROR|Invalid LOGIN format";
            }
        }

        // REGISTER
        else if (
                message.startsWith(
                        "REGISTER"
                )
        ) {

            String[] data =
                    message.split("\\|");

            if (data.length == 6) {

                String fullname =
                        data[1].trim();

                String username =
                        data[2].trim();

                String email =
                        data[3].trim();

                String password =
                        data[4].trim();

                String dob =
                        data[5].trim();

                System.out.println(
                        fullname + " | "
                                + username + " | "
                                + email
                );

                UserDAO userDAO =
                        new UserDAO();

                if (
                        userDAO.findByEmail(email)
                                != null
                ) {

                    System.out.println(
                            "Email already exists"
                    );

                    return "REGISTER_FAILED";
                }

                if (
                        userDAO.findByUsername(username)
                                != null
                ) {

                    System.out.println(
                            "Username already exists"
                    );

                    return "REGISTER_FAILED";
                }

                User user =
                        new User();

                String shortId =
                        "USR"
                                + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 7)
                                .toUpperCase();

                user.setId(shortId);

                user.setFullname(
                        fullname
                );

                user.setUsername(
                        username
                );

                user.setEmail(
                        email
                );

                user.setPassword(
                        password
                );

                user.setDob(
                        dob
                );

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

                return "ERROR|Invalid REGISTER format";
            }
        }

        // GET AUCTIONS
        else if (
                message.startsWith(
                        "GET_AUCTIONS"
                )
        ) {

            return "AUCTION_LIST_DATA";
        }

        return "ERROR|Unknown Command";
    }

    private void closeConnection() {

        try {

            if (reader != null) {

                reader.close();
            }

            if (writer != null) {

                writer.close();
            }

            if (
                    socket != null
                            && !socket.isClosed()
            ) {

                socket.close();
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}