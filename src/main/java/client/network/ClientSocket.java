package client.network;

import client.manager.UserSession;
import client.network.response.ResponseHandler;
import common.Command;
import common.CommandBuilder;
import model.Auction;
import model.Entity.Item.Item;
import model.Entity.User.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientSocket {

    private static final String HOST = "26.175.50.93";
    private static final int PORT = 9999;

    private static final Logger LOGGER =
            Logger.getLogger(ClientSocket.class.getName());

    private static volatile ClientSocket instance;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private boolean listening = false;
    private final java.util.Map<String, java.util.function.Consumer<String>> listeners
            = new java.util.concurrent.ConcurrentHashMap<>();

    public static String currentRequest = "";

    // ================= CONSTRUCTOR =================

    private ClientSocket() throws Exception {
        connect();
    }

    public static ClientSocket getInstance() {
        if (instance == null) {
            synchronized (ClientSocket.class) {
                if (instance == null) {
                    try {
                        instance = new ClientSocket();
                    } catch (Exception e) {
                        LOGGER.severe(
                                "Cannot connect server: "
                                        + e.getMessage()
                        );
                        return null;
                    }
                }
            }
        }
        return instance;
    }

    private void connect() throws Exception {
        if (socket == null || socket.isClosed()) {

            socket = new Socket(HOST, PORT);

            in =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()
                            )
                    );

            out =
                    new PrintWriter(
                            socket.getOutputStream(),
                            true
                    );

            System.out.println(
                    "Connected to "
                            + HOST
                            + ":"
                            + PORT
            );
        }
    }

    // ================= LISTEN =================

    public void listen() {

        if (listening) return;

        listening = true;

        Thread t =
                new Thread(() -> {
                    try {
                        String msg;
                        while (
                                listening
                                        &&
                                        (msg = in.readLine()) != null
                        ) {
                            System.out.println(
                                    "FROM SERVER: "
                                            + msg
                            );

                            ResponseHandler.handle(msg);
                            // Chỉ notify listeners với message KHÔNG phải live auction
                            if (!isNonBroadcastMessage(msg)) {
                                notifyListeners(msg);
                            }
                        }

                    } catch (Exception e) {
                        if (listening) {
                            System.err.println(
                                    "Connection lost: "
                                            + e.getMessage()
                            );
                            ResponseHandler.handle(
                                    "DISCONNECTED"
                            );
                        }

                    } finally {
                        listening = false;
                        close();
                    }

                });

        t.setDaemon(true);

        t.start();
    }

    private boolean isNonBroadcastMessage(String msg) {
        return msg.startsWith("UPDATE_PRICE")
                || msg.startsWith("JOIN_SUCCESS")
                || msg.startsWith("JOIN_FAILED")
                || msg.startsWith("BID_FAILED")
                || msg.startsWith("BID_HISTORY_SUCCESS")
                || msg.startsWith("BID_HISTORY_EMPTY")
                || msg.startsWith("AUCTION_ENDED")
                || msg.startsWith("YOU_WON")
                || msg.startsWith("VIRTUAL_BALANCE")
                || msg.startsWith("WINNER_BALANCE")
                || msg.startsWith("SELLER_BALANCE")
                || msg.startsWith("TIME_EXTENDED")
                || msg.startsWith("AUTO_BID_SET")
                || msg.startsWith("AUTO_BID_CANCELLED")
                || msg.startsWith("AUTO_BID_MAX_REACHED")
                || msg.startsWith("ERROR");
    }
    // ================= SEND =================
    public synchronized void sendMessage(String rawMessage) {
        try {
            connect();
            out.println(rawMessage);
            out.flush();
            System.out.println(
                    "REQUEST: "
                            + rawMessage
            );

        } catch (Exception e) {
            LOGGER.severe(
                    "Send failed: "
                            + e.getMessage()
            );
        }
    }

    public void sendRequest(String raw) {
        sendMessage(raw);
    }

    private void send(Command command, String... data) {
        String msg =
                CommandBuilder.build(
                        command,
                        data
                );
        sendMessage(msg);
    }


    // AUTH
    public void sendLogin(
            String username,
            String password
    ) {

        send(
                Command.LOGIN,
                username.trim(),
                password.trim()
        );
    }

    public void sendRegister(
            String fn,
            String un,
            String em,
            String pw,
            String dob
    ) {

        send(
                Command.REGISTER,
                fn, un, em, pw, dob
        );
    }

    public void sendLogout() {
        send(Command.LOGOUT);
    }

    public void sendForgotPassword(
            String fullName,
            String dob,
            String username,
            String email,
            String newPassword
    ) {

        send(
                Command.FORGOT_PASSWORD,
                fullName,
                dob,
                username,
                email,
                newPassword
        );
    }

    // AUCTION
    public void sendList() {
        currentRequest = "LIST";
        send(Command.LIST);
    }

    public void sendCreate(Auction auction) {
        Item item = auction.getItem();
        String images =
                item.getImages() != null
                        &&
                        !item.getImages().isEmpty()

                        ? String.join(
                        ",",
                        item.getImages()
                )

                        : "NO_IMAGE";

        send(
                Command.CREATE,
                auction.getAuction_id(),
                item.getItem_id(),
                item.getName(),
                item.getDescription(),
                item.getCategory().name(),
                images,
                auction.getCurrentPrice().toString(),
                auction.getMinIncrement().toString(),
                auction.getStartTime().toString(),
                auction.getEndTime().toString()
        );
    }

    public void sendBid(String auctionId, String amount) {
        send(Command.BID, auctionId, amount);
    }

    public void sendJoin(String auctionId) {
        send(Command.JOIN, auctionId);
    }

    public void sendLeave() {
        send(Command.LEAVE);
    }

    public void sendGetBidHistory(String auctionId) {
        send(Command.GET_BID_HISTORY, auctionId);
    }

    public void sendCreatedAuctions() {
        User user = UserSession.getCurrentUser();
        if (user == null) return;
        System.out.println("SEND CREATED AUCTIONS");
        currentRequest = "LIST_CREATED_AUCTIONS";
        sendMessage("LIST_CREATED_AUCTIONS|" + user.getUser_id());
    }

    // BALANCE
    public void sendDeposit(String userId, BigDecimal amount) {
        sendMessage(
                "DEPOSIT|"
                        + userId
                        + "|"
                        + amount
        );
    }

    public void sendWithdraw(String userId, BigDecimal amount) {
        sendMessage(
                "WITHDRAW|"
                        + userId
                        + "|"
                        + amount
        );
    }

    public void sendGetTransactions(String userId) {
        sendMessage("GET_TRANSACTIONS|" + userId);
    }

    // CONNECTION
    public void logout() {
        send(Command.LOGOUT);
    }

    public void close() {
        try {
            listening = false;

            if (in != null)
                in.close();

            if (out != null)
                out.close();

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            System.out.println("Socket closed");

        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }


    public void addListener(String key, java.util.function.Consumer<String> listener) {
        listeners.put(key, listener);
    }

    public void removeListener(String key) {
        listeners.remove(key);
    }

    public void notifyListeners(String msg) {
        listeners.values().forEach(l -> l.accept(msg));
    }

    public void sendGetVirtualBalance(String userId) {
        sendMessage("GET_VIRTUAL_BALANCE|" + userId);
    }

    public void sendSetAutoBid(String auctionId, String maxAmount) {
        send(Command.SET_AUTO_BID, auctionId, maxAmount);
    }
    public void sendCancelAutoBid(String auctionId) {
        send(Command.CANCEL_AUTO_BID, auctionId);
    }
}