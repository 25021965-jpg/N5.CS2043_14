package client.network;

import client.manager.UserSession;
import client.network.response.ResponseHandler;
import common.Command;
import common.CommandBuilder;
import model.Auction;
import model.Item;
import model.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;

public class ClientSocket {

    private static ClientSocket instance;

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
            try {
                instance = new ClientSocket();
            } catch (Exception e) {

                System.err.println(
                        "Cannot connect server: "
                                + e.getMessage()
                );
                return null;
            }
        }
        return instance;
    }

    private void connect() throws Exception {
        if (socket == null || socket.isClosed()) {
            socket =
                    new Socket("localhost", 9999);

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
                    "Connected to localhost:9999"
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
                            notifyListeners(msg);
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
            System.err.println(
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

    public void sendMyAuctions() {
        User user = UserSession.getCurrentUser();
        if (user == null) return;
        currentRequest = "LIST_MY_AUCTIONS";
        sendMessage("LIST_MY_AUCTIONS|" + user.getUser_id());
    }

    // FAVOURITES
    public void sendGetFavourite(String userId) {
        sendMessage("LIST_FAVOURITES|" + userId);
    }

    public void sendAddFavourite(String userId, String auctionId) {
        sendMessage(
                "ADD_FAVOURITE|"
                        + userId
                        + "|"
                        + auctionId
        );
    }

    public void sendRemoveFavourite(
            String userId,
            String auctionId
    ) {

        sendMessage(
                "REMOVE_FAVOURITE|"
                        + userId
                        + "|"
                        + auctionId
        );
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

    public void sendGetBalance(String userId) {
        sendMessage("GET_BALANCE|" + userId);
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
            System.err.println(e.getMessage());
        }
    }


    public void setMessageListener(java.util.function.Consumer<String> listener) {
        listeners.put("default", listener);
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
}