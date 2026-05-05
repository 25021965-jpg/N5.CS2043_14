package server.network;

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

        if (message.startsWith("LOGIN")) {
            return "AUTH_SUCCESS";
        } else if (message.startsWith("GET_AUCTIONS")) {
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