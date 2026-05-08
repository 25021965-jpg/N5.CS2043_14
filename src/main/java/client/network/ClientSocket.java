package client.network;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ClientSocket {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientSocket() throws Exception {
        socket = new Socket("localhost", 9999);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

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

    private void send(String msg) {
        out.println(msg);
    }

    // SỬA: LOGIN username password (dùng dấu cách)
    public void sendLogin(String username, String password) {
        send("LOGIN " + username + " " + password);
    }

    // SỬA: REGISTER fullname username email password
    public void sendRegister(String fullname, String username,
                             String email, String password) {

        send("REGISTER|" + fullname + "|" +
                username + "|" +
                email + "|" +
                password);

        System.out.println(
                "SEND: REGISTER|" + fullname + "|" +
                        username + "|" +
                        email + "|" +
                        password
        );
    }

    // GIỮ NGUYÊN các method khác
    public void sendLogout() {
        send("LOGOUT");
    }

    public void sendList() {
        send("LIST");
    }

    public void sendJoin(String id) {
        send("JOIN " + id);
    }

    public void sendBid(String auctionId, String amount) {
        send("BID " + auctionId + " " + amount);
    }

    public void sendCreate(String name, String category, String price) {
        send("CREATE " + name + " " + category + " " + price);
    }
}