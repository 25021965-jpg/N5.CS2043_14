package client.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

import common.Command;

public class ClientSocket {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    public ClientSocket() throws Exception {

        socket = new Socket("localhost", 9999);
        in = new BufferedReader(
                new InputStreamReader(
                        socket.getInputStream()
                )
        );
        out = new PrintWriter(
                socket.getOutputStream(),
                true
        );
    }

    // lắng nghe message từ server
    public void listen(Consumer<String> callback) {
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    callback.accept(msg);
                }

            } catch (Exception e) {
                callback.accept("DISCONNECTED");
            }
        }).start();
    }

    // generic send
    private void send(Command command, String... data) {
        StringBuilder sb =
                new StringBuilder(command.name());
        for (String s : data) {
            sb.append("|").append(s);
        }
        String msg = sb.toString();
        out.println(msg);
        System.out.println("SEND: " + msg);
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
            String password
    ) {

        send(
                Command.REGISTER,
                fullname,
                username,
                email,
                password
        );
    }

    // LOGOUT
    public void sendLogout() {

        send(Command.LOGOUT);
    }

    // LIST
    public void sendList() {

        send(Command.LIST);
    }

    // JOIN
    public void sendJoin(String auctionId) {

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

    // đóng kết nối
    public void close() {

        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null &&
                    !socket.isClosed()) {
                socket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}