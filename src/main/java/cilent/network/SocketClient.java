package client;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class SocketClient {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public SocketClient() throws Exception {
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
                callback.accept("Disconnected from server");
            }
        }).start();
    }

    private void send(String msg) {
        out.println(msg);
    }

    public void sendLogin(String email, String password) {
        send("LOGIN " + email + " " + password);
    }

    public void sendRegister(String username, String email, String password, String role) {
        send("REGISTER " + username + " " + email + " " + password + " " + role);
    }

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
        out.println("BID " + auctionId + " " + amount);
    }

    public void sendCreate(String name, String category, String price) {
        send("CREATE " + name + " " + category + " " + price);
    }
}