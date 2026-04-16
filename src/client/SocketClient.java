package client;

import java.io.*;
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

    // gửi bid từ UI
    public void sendBid(String bid){
        out.println(bid);
    }

    // nhận realtime từ server
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