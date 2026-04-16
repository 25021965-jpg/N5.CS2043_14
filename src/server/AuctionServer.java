package server;

import model.*;
import service.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.math.BigDecimal;
import java.util.*;

public class AuctionServer {

    private static List<PrintWriter> clients = new ArrayList<>();
    private static AuthService authService = new AuthService();

    public static void main(String[] args) throws Exception {

        ServerSocket server = new ServerSocket(9999);
        System.out.println("Server running on port 9999...");

        // fake auction
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

    private static void handleClient(Socket client, String clientId,
                                     Auction auction, BidService bidService) {

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

                // LOGIN
                if (msg.startsWith("LOGIN")) {
                    String[] parts = msg.split(" ");
                    String username = parts[1];
                    String password = parts[2];

                    User user = authService.login(username, password);

                    if (user != null) {
                        authService.addSession(clientId, user);
                        out.println("LOGIN_SUCCESS");
                    } else {
                        out.println("LOGIN_FAIL");
                    }
                    continue;
                }

                // BID
                User user = authService.getUser(clientId);

                if (user == null) {
                    out.println("ERROR: Please login first");
                    continue;
                }

                try {
                    BigDecimal amount = new BigDecimal(msg);

                    Bid bid = bidService.placeBid(user, auction, amount);

                    broadcast("NEW BID: " + bid.getAmount());

                    out.println("SUCCESS: " + bid.getAmount());

                } catch (Exception e) {
                    out.println("ERROR: " + e.getMessage());
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
            } catch (Exception e) {}
        }
    }

    private static void broadcast(String message) {
        for (PrintWriter client : clients) {
            client.println(message);
        }
    }
}