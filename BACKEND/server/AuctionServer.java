package server;

import model.Auction;
import model.AuctionStatus;
import model.Bid;
import model.Role;
import model.User;
import service.AuthService;
import service.BidService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AuctionServer {

    private static final List<PrintWriter> clients = new ArrayList<>();
    private static final AuthService authService = new AuthService();

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(9999);
        System.out.println("Server running on port 9999...");

        Auction auction = new Auction();
        auction.setCurrentPrice(BigDecimal.valueOf(100));
        auction.setMinIncrement(BigDecimal.valueOf(10));
        auction.setStatus(AuctionStatus.ACTIVE);

        BidService bidService = new BidService();

        while (true) {
            Socket client = server.accept();
            String clientId = client.getInetAddress() + ":" + client.getPort();
            System.out.println("Client " + clientId + " connected");

            new Thread(() -> handleClient(client, clientId, auction, bidService)).start();
        }
    }

    private static void handleClient(Socket client, String clientId, Auction auction, BidService bidService) {
        PrintWriter out = null;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
            clients.add(out);

            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("[" + clientId + "] " + msg);

                if (msg.startsWith("LOGIN ")) {
                    handleLogin(clientId, msg, out);
                    continue;
                }

                if (msg.startsWith("REGISTER ")) {
                    handleRegister(msg, out);
                    continue;
                }

                if (msg.equals("LOGOUT")) {
                    authService.logout(clientId);
                    out.println("LOGOUT_SUCCESS");
                    continue;
                }

                if (msg.equals("PING")) {
                    out.println("PONG");
                    continue;
                }

                User user = authService.getUser(clientId);
                if (user == null) {
                    out.println("ERROR: Please login first");
                    continue;
                }

                if (!user.hasRole(Role.BIDDER)) {
                    out.println("ERROR: Not allowed to bid");
                    continue;
                }

                if (msg.startsWith("BID ")) {
                    try {
                        String[] parts = msg.split(" ");
                        BigDecimal amount = new BigDecimal(parts[1]);

                        Bid bid = bidService.placeBid(user, auction, amount);

                        broadcast("NEW BID: " + bid.getAmount() + " by " + user.getUsername());
                        out.println("SUCCESS: " + bid.getAmount());

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
            } catch (Exception ignored) {
            }
        }
    }

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
            out.println("ERROR: LOGIN format -> LOGIN email password");
        }
    }

    private static void handleRegister(String msg, PrintWriter out) {
        try {
            String[] parts = msg.split(" ");
            String username = parts[1];
            String email = parts[2];
            String password = parts[3];
            String roleText = parts[4].toUpperCase();

            Role role = Role.valueOf(roleText);

            String id = String.valueOf(authService.getUsers().size() + 1);
            User user = authService.register(id, username, email, password, List.of(role));

            if (user != null) {
                out.println("REGISTER_SUCCESS");
            } else {
                out.println("REGISTER_FAIL: email already exists");
            }
        } catch (Exception e) {
            out.println("ERROR: REGISTER format -> REGISTER username email password role");
        }
    }

    private static void broadcast(String message) {
        for (PrintWriter client : clients) {
            client.println(message);
        }
    }
}