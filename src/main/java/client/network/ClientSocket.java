package client.network;

import common.Command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import java.util.function.Consumer;

public class ClientSocket {

    private Socket socket;

    private BufferedReader in;

    private PrintWriter out;

    private boolean listening = false;

    public ClientSocket() throws Exception {

        socket =
                new Socket(
                        "localhost",
                        9999
                );

        in =
                new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()
                        )
                );

        out =
                new PrintWriter(
                        socket.getOutputStream(),
                        true
                );

        System.out.println(
                "Connected to server"
        );
    }

    // LISTEN SERVER

    public void listen(
            Consumer<String> callback
    ) {

        // tránh tạo nhiều thread listen
        if (listening) {
            return;
        }

        listening = true;

        new Thread(() -> {

            try {

                String msg;

                while (
                        (msg = in.readLine()) != null
                ) {

                    System.out.println(
                            "FROM SERVER: "
                                    + msg
                    );

                    callback.accept(msg);
                }

            } catch (Exception e) {

                e.printStackTrace();

                callback.accept(
                        "DISCONNECTED"
                );

            } finally {

                listening = false;
            }

        }).start();
    }

    // GENERIC SEND

    private void send(
            Command command,
            String... data
    ) {

        if (
                socket == null
                        || socket.isClosed()
        ) {

            System.out.println(
                    "Socket closed!"
            );

            return;
        }

        StringBuilder sb =
                new StringBuilder(
                        command.name()
                );

        for (String s : data) {

            sb.append("|")
                    .append(s);
        }

        String msg =
                sb.toString();

        out.println(msg);

        out.flush();

        System.out.println(
                "SEND: " + msg
        );
    }

    // LOGIN

    public void sendLogin(
            String username,
            String password
    ) {

        send(
                Command.LOGIN,
                username,
                password
        );
    }

    // REGISTER

    public void sendRegister(
            String fullname,
            String username,
            String email,
            String password,
            String dob
    ) {

        send(
                Command.REGISTER,
                fullname,
                username,
                email,
                password,
                dob
        );
    }

    // LOGOUT

    public void sendLogout() {

        send(
                Command.LOGOUT
        );
    }

    // LIST

    public void sendList() {

        send(
                Command.LIST
        );
    }

    // JOIN

    public void sendJoin(
            String auctionId
    ) {

        send(
                Command.JOIN,
                auctionId
        );
    }

    // BID

    public void sendBid(
            String auctionId,
            String amount
    ) {

        send(
                Command.BID,
                auctionId,
                amount
        );
    }

    // CREATE

    public void sendCreate(
            String name,
            String category,
            String price
    ) {

        send(
                Command.CREATE,
                name,
                category,
                price
        );
    }

    // CLOSE

    public void close() {

        try {

            listening = false;

            if (in != null) {

                in.close();
            }

            if (out != null) {

                out.close();
            }

            if (
                    socket != null
                            && !socket.isClosed()
            ) {

                socket.close();
            }

            System.out.println(
                    "Socket closed"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}