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
        connect();
    }

    private void connect() throws Exception {
        if (socket == null || socket.isClosed()) {
            socket = new Socket("localhost", 9999);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("✓ Connected to server at localhost:9999");
        }
    }

    public static ClientSocket getInstance() {
        if (instance == null) {
            try {
                instance = new ClientSocket();
            } catch (Exception e) {
                System.err.println("✕ Could not connect to server: " + e.getMessage());
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
                    // Chuyển dữ liệu cho ResponseHandler xử lý giao diện
                    ResponseHandler.handle(msg);
                }
            } catch (Exception e) {
                if (listening) {
                    System.err.println("✕ Connection lost: " + e.getMessage());
                    ResponseHandler.handle("DISCONNECTED");
                }
            } finally {
                listening = false;
                close();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // --- GENERIC SEND ---
    private void send(Command command, String... data) {
        try {
            connect(); // Đảm bảo duy trì socket

            String msg = CommandBuilder.build(command, data);
            out.println(msg);
            out.flush();
            System.out.println("→ SENT: " + msg);
        } catch (Exception e) {
            System.err.println("✕ Send error: " + e.getMessage());
        }
    }

    public void sendLogin(String username, String password) {
        send(Command.LOGIN, username.trim(), password.trim());
    }

    public void sendRegister(String fn, String un, String em, String pw, String dob) {
        send(Command.REGISTER, fn, un, em, pw, dob);
    }

    public void sendLogout() {
        send(Command.LOGOUT);
    }
    public void sendForgotPassword(String fullName, String dob, String username, String email, String newPassword) {
        send(Command.FORGOT_PASSWORD, fullName, dob, username, email, newPassword);
    }

    public void sendList() {
        send(Command.LIST);
    }

    public void sendBid(String auctionId, String amount) {
        send(Command.BID, auctionId, amount);
    }

    public void sendCreate(Auction auction) {
        Item item = auction.getItem();

        // Xử lý danh sách ảnh thành chuỗi "url1,url2" hoặc "NO_IMAGE"
        String images = "NO_IMAGE";
        if (item.getImages() != null && !item.getImages().isEmpty()) {
            images = String.join(",", item.getImages());
        }

        // Lọc sạch dữ liệu để tránh lỗi split "|" ở Server
        String cleanName = item.getName().replace("|", "-");
        String cleanDesc = item.getDescription().replace("|", "-").replace("\n", " ");

        send(Command.CREATE,
                auction.getAuction_id(),           // data[1]
                item.getItem_id(),                 // data[2]
                cleanName,                         // data[3]
                cleanDesc,                         // data[4]
                item.getCategory().name(),         // data[5]
                images,                            // data[6]
                auction.getCurrentPrice().toString(), // data[7]
                auction.getMinIncrement().toString(), // data[8]
                auction.getStartTime().toString(), // data[9]
                auction.getEndTime().toString()    // data[10]
        );
    }

    public void close() {
        try {
            listening = false;
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("✓ Socket closed safely");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());        }
    }
}