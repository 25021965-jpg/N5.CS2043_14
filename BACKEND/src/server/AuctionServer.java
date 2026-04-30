package server;

import model.*;
import service.*;

import java.io.*;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class AuctionServer {

    private static final List<PrintWriter> clients = new ArrayList<>();
    private static final AuthService authService = new AuthService();
    private static final AuctionService auctionService = new AuctionService();
    private static final BidService bidService = new BidService();

    public static void main(String[] args) throws Exception {

        ServerSocket server = new ServerSocket(9999);
        System.out.println("Server running on port 9999...");

        while (true) {
            Socket client = server.accept();
            String clientId = client.getInetAddress() + ":" + client.getPort();

            System.out.println("Client connected: " + clientId);

            new Thread(() -> handleClient(client, clientId)).start();
        }
    }

    private static void handleClient(Socket client, String clientId) {

        PrintWriter out = null;

        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(client.getInputStream())
            );

            out = new PrintWriter(client.getOutputStream(), true);
            clients.add(out);

            String msg;

            while ((msg = in.readLine()) != null) {

                System.out.println("[" + clientId + "] " + msg);

                // ===== LOGIN =====
                if (msg.startsWith("LOGIN ")) {
                    handleLogin(clientId, msg, out);
                    continue;
                }

                // ===== REGISTER =====
                if (msg.startsWith("REGISTER ")) {
                    handleRegister(msg, out);
                    continue;
                }

                // ===== LOGOUT =====
                if (msg.equals("LOGOUT")) {
                    authService.logout(clientId);
                    out.println("LOGOUT_SUCCESS");
                    continue;
                }

                // ===== LIST =====
                if (msg.equals("LIST")) {
                    for (Auction a : auctionService.getAllAuctions()) {
                        out.println("AUCTION " + a.getId() + " PRICE " + a.getCurrentPrice());
                    }
                    continue;
                }

                // ===== JOIN =====
                if (msg.startsWith("JOIN ")) {
                    String id = msg.split(" ")[1];

                    Auction auction = auctionService.getAuctionById(id);

                    if (auction == null) {
                        out.println("ERROR: Auction not found");
                    } else {
                        out.println("JOINED " + id);
                        out.println("CURRENT PRICE: " + auction.getCurrentPrice());
                    }
                    continue;
                }

                // ===== CREATE =====
                if (msg.startsWith("CREATE ")) {
                    handleCreate(clientId, msg, out);
                    continue;
                }

                // ===== BID =====
                if (msg.startsWith("BID ")) {

                    User user = authService.getUser(clientId);

                    if (user == null) {
                        out.println("ERROR: Please login first");
                        continue;
                    }

                    if (!user.hasRole(Role.BIDDER)) {
                        out.println("ERROR: Not allowed to bid");
                        continue;
                    }

                    try {
                        String[] parts = msg.split(" ");
                        String auctionId = parts[1];
                        BigDecimal amount = new BigDecimal(parts[2]);

                        Auction auction = auctionService.getAuctionById(auctionId);

                        if (auction == null) {
                            out.println("ERROR: Auction not found");
                            continue;
                        }

                        Bid bid = bidService.placeBid(user, auction, amount);

                        auctionService.saveAll();

                        broadcast("NEW BID: " + bid.getAmount()
                                + " by " + user.getUsername()
                                + " (Auction " + auctionId + ")");

                        out.println("SUCCESS");

                    } catch (Exception e) {
                        out.println("ERROR: " + e.getMessage());
                    }

                    continue;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (out != null) {
                clients.remove(out);
            }

            authService.logout(clientId);

            try {
                client.close();
            } catch (Exception ignored) {}
        }
    }

    // ===== LOGIN =====
    private static void handleLogin(String clientId, String msg, PrintWriter out) {

        try {
            String[] parts = msg.split(" ");
            String email = parts[1];
            String password = parts[2];

            User user = authService.login(email, password);

            if (user != null) {
                authService.addSession(clientId, user);
                out.println("LOGIN_SUCCESS " + user.getUsername());
            } else {
                out.println("LOGIN_FAIL");
            }

        } catch (Exception e) {
            out.println("ERROR: LOGIN email password");
        }
    }

    // ===== REGISTER =====
    private static void handleRegister(String msg, PrintWriter out) {

        try {
            String[] parts = msg.split(" ");

            String username = parts[1];
            String email = parts[2];
            String password = parts[3];

            String id = String.valueOf(authService.getUsers().size() + 1);

            User user = authService.register(
                    id, username, email, password,
                    List.of(Role.BIDDER)
            );

            if (user != null) {
                out.println("REGISTER_SUCCESS");
            } else {
                out.println("REGISTER_FAIL");
            }

        } catch (Exception e) {
            out.println("ERROR: REGISTER username email password");
        }
    }

    // ===== CREATE =====
    private static void handleCreate(String clientId, String msg, PrintWriter out) {

        try {
            User user = authService.getUser(clientId);

            if (user == null) {
                out.println("ERROR: login first");
                return;
            }

            if (!user.hasRole(Role.SELLER)) {
                out.println("ERROR: not seller");
                return;
            }

            String[] parts = msg.split(" ");
            String itemName = parts[1];
            BigDecimal price = new BigDecimal(parts[2]);

            Item item = new Item();
            item.setName(itemName);

            Auction auction = auctionService.createAuction(user, item, price);

            out.println("CREATED " + auction.getId());

        } catch (Exception e) {
            out.println("ERROR: CREATE item price");
        }
    }

    // ===== BROADCAST =====
    private static void broadcast(String message) {
        for (PrintWriter client : clients) {
            client.println(message);
        }
    }
}