package client.network;

import common.*;
import model.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.function.Consumer;

public class ClientSocket {

    private static ClientSocket instance;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean listening = false;
    private Consumer<String> messageListener = null;
    public static String currentRequest = "";

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
                    System.out.println("FROM SERVER: " + msg);

                    if (messageListener != null) {
                        messageListener.accept(msg);
                    }

                    ResponseHandler.handle(msg);
                }
            } catch (Exception e) {
                if (listening) {
                    System.err.println("✕ Connection lost: " + e.getMessage());
                    if (messageListener != null) {
                        messageListener.accept("DISCONNECTED");
                    }
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

    // Gửi message bất kỳ dưới dạng raw string
    public void sendMessage(String rawMessage) {
        try {
            connect();
            if (out != null) {
                out.println(rawMessage);
                out.flush();
                System.out.println("→ REQUEST SENT: " + rawMessage);
            }
        } catch (Exception e) {
            System.err.println("✕ Send error: " + e.getMessage());
        }
    }

    public void sendRequest(String rawMessage) {
        sendMessage(rawMessage);
    }

    private void send(Command command, String... data) {
        String msg = CommandBuilder.build(command, data);
        sendMessage(msg);
    }

    // ========== AUTH ==========

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

    // ========== AUCTION ==========

    public void sendJoin(String auctionId) {
        send(Command.JOIN, auctionId);
    }

    public void sendLeave() {
        send(Command.LEAVE);
    }

    public void sendGetBidHistory(String auctionId) {
        send(Command.GET_BID_HISTORY, auctionId);
    }

    public void sendList() {
        currentRequest = "LIST";
        send(Command.LIST);
    }
    public void sendBid(String auctionId, String amount) {
        send(Command.BID, auctionId, amount);
    }

    public void sendCreate(Auction auction) {
        Item item = auction.getItem();
        String images = (item.getImages() != null && !item.getImages().isEmpty())
                ? String.join(",", item.getImages()) : "NO_IMAGE";

        String cleanName = item.getName().replace("|", "-");
        String cleanDesc = item.getDescription().replace("|", "-").replace("\n", " ");

        send(Command.CREATE,
                auction.getAuction_id(),
                item.getItem_id(),
                cleanName,
                cleanDesc,
                item.getCategory().name(),
                images,
                auction.getCurrentPrice().toString(),
                auction.getMinIncrement().toString(),
                auction.getStartTime().toString(),
                auction.getEndTime().toString()
        );
    }

    // ========== BALANCE ==========

    public void sendDeposit(String userId, BigDecimal amount) {
        sendMessage("DEPOSIT|" + userId + "|" + amount);
    }

    public void sendWithdraw(String userId, BigDecimal amount) {
        sendMessage("WITHDRAW|" + userId + "|" + amount);
    }

    public void sendGetTransactions(String userId) {
        sendMessage("GET_TRANSACTIONS|" + userId);
    }

    public void sendGetBalance(String userId) {
        sendMessage("GET_BALANCE|" + userId);
    }

    // ========== CONNECTION ==========

    public void logout() {
        send(Command.LOGOUT);
    }

    public void close() {
        try {
            listening = false;
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("✓ Socket closed safely");
        } catch (Exception e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }

    public void setMessageListener(Consumer<String> listener) {
        this.messageListener = listener;
    }
}
