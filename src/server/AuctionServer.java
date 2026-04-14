package server;

import java.util.ArrayList;
import java.util.List;
import model.*;
import service.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.math.BigDecimal;

import static java.lang.System.out;

public class AuctionServer {

    private static List<PrintWriter> clients = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        ServerSocket server = new ServerSocket(9999);
        out.println("Server running on port 9999...");

        // fake data (test)
        User user = new User();
        user.setRoles(List.of(Role.BIDDER));

        Auction auction = new Auction();
        auction.setCurrentPrice(BigDecimal.valueOf(100));
        auction.setMinIncrement(BigDecimal.valueOf(10));
        auction.setStatus(AuctionStatus.ACTIVE);

        BidService bidService = new BidService();

        while (true) {
            Socket client = server.accept();

            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(client.getInputStream())
                    );

                    PrintWriter out = new PrintWriter(
                            client.getOutputStream(), true
                    );

                    String request = in.readLine();

                    try {
                        BigDecimal amount = new BigDecimal(request);

                        System.out.println("Client " + client + " bid: " + amount);

                        Bid bid = bidService.placeBid(user, auction, amount);

                        out.println("SUCCESS: " + bid.getAmount());

                    } catch (Exception e) {
                        out.println("ERROR: " + e.getMessage());
                    }

                    client.close();

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    clients.remove(out);
                    try {
                        client.close();
                    } catch (Exception e) {}
                }
            }).start();

            System.out.println("Client " + client.getPort() + " connected");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(client.getInputStream())
            );

            PrintWriter out = new PrintWriter(
                    client.getOutputStream(), true
            );

            clients.add(out);

            String request = in.readLine();
            System.out.println("Received: " + request);

            try {
                BigDecimal amount = new BigDecimal(request);

                Bid bid = bidService.placeBid(user, auction, amount);

                broadcast("Client " + client.getPort() + " bid: " + amount);

                out.println("SUCCESS: " + bid.getAmount());

            } catch (Exception e) {
                out.println("ERROR: " + e.getMessage());
            }

            client.close();
        }
    }
    private static void broadcast(String message) {
        for (PrintWriter client : clients) {
            client.println(message);
        }
    }
}