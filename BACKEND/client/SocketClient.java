package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public SocketClient() throws Exception {
        socket = new Socket("localhost", 9999);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void send(String message) {
        out.println(message);
    }

    public void sendBid(String amount) {
        send("BID " + amount);
    }

    public void sendLogin(String email, String password) {
        send("LOGIN " + email + " " + password);
    }

    public void listen(MessageListener listener) {
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    listener.onMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public interface MessageListener {
        void onMessage(String msg);
    }
}