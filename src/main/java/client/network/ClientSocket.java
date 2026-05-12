package client.network;

import common.*;
import model.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSocket {

    private static ClientSocket instance;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean listening = false;

    private ClientSocket() throws Exception {
        if (socket == null || socket.isClosed()) {
            socket = new Socket("localhost", 9999);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connected to server");
        }
    }

    public static ClientSocket getInstance() {
        if (instance == null) {
            try {
                instance = new ClientSocket();
            } catch (Exception e) {
                System.err.println("Could not connect to server: " + e.getMessage());
                return null;
            }
        }
        return instance;
    }

    // --- LISTEN SERVER ---
    public void listen() {
        if (listening) return;
        listening = true;

        Thread t = new Thread(() -> {
            try {
                String msg;
                while (listening && (msg = in.readLine()) != null) {
                    System.out.println("FROM SERVER: " + msg);
                    ResponseHandler.handle(msg);
                }
            } catch (Exception e) {
                if (listening) {
                    System.err.println("Connection error: " + e.getMessage());
                    ResponseHandler.handle("DISCONNECTED");
                }
            } finally {
                listening = false;
            }
        });
        t.setDaemon(true); // Luồng này tự tắt khi đóng App
        t.start();
    }

    // --- GENERIC SEND ---
    private void send(Command command, String... data) {
        if (socket == null || socket.isClosed()) {
            System.out.println("Socket closed!");
            return;
        }
        String msg = CommandBuilder.build(command, data);
        out.println(msg);
        out.flush();
        System.out.println("SEND: " + msg);
    }

    public void sendLogin(String username, String password) { send(Command.LOGIN, username, password); }
    public void sendRegister(String fn, String un, String em, String pw, String dob) { send(Command.REGISTER, fn, un, em, pw, dob); }
    public void sendLogout() { send(Command.LOGOUT); }
    public void sendList() { send(Command.LIST); }
    public void sendJoin(String auctionId) { send(Command.JOIN, auctionId); }
    public void sendBid(String auctionId, String amount) { send(Command.BID, auctionId, amount); }

    public void sendCreate(Auction auction) {
        Item item = auction.getItem();
        String images = (item.getImages() != null) ? String.join(",", item.getImages()) : "";
        send(Command.CREATE, auction.getId(), item.getId(), item.getName(), item.getDescription(),
                item.getCategory().name(), images, auction.getCurrentPrice().toString(),
                auction.getMinIncrement().toString(), auction.getStartTime().toString(), auction.getEndTime().toString());
    }

    public void close() {
        try {
            listening = false;
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("Socket closed");
        } catch (Exception e) { e.printStackTrace(); }
    }
}