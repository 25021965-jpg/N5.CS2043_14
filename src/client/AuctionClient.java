package client;

import java.io.*;
import java.net.Socket;

public class AuctionClient {

    static void main(String[] args) throws Exception {

        Socket socket = new Socket("localhost", 9999);

        PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true
        );

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        // gửi bid
        out.println("120");
        out.println("105");

        while (true) {
            String response = in.readLine();
            if (response == null) break;

            System.out.println("Server: " + response);
        }

        socket.close();
    }
}
