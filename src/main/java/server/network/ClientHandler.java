package server.network;

import common.Command;
import model.Entity.User.User;
import server.manager.RoomManager;
import server.network.handler.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private final Socket socket;

    private BufferedReader reader;
    private PrintWriter writer;

    private User currentUser;
    private String currentAuctionId;

    private static final Logger LOGGER =
            Logger.getLogger(ClientHandler.class.getName());

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            writer = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

            System.out.println("[NEW CONNECTION] "
                    + socket.getInetAddress());

            String message;

            while ((message = reader.readLine()) != null) {

                System.out.println("REQUEST: " + message);

                String response = handleRequest(message);

                writer.println(response);

                System.out.println("RESPONSE: " + response);
            }

        } catch (Exception e) {
            System.out.println("Client disconnected.");
        } finally {
            closeConnection();
        }
    }

    private String handleRequest(String message) {

        try {
            String[] data = message.split("\\|");

            if (data.length == 0)
                return "ERROR|Empty command";

            Command cmd = Command.valueOf(
                    data[0].trim().toUpperCase()
            );

            AuthHandler authHandler = new AuthHandler(
                    currentUser,
                    writer,
                    currentAuctionId
            );

            AuctionHandler auctionHandler = new AuctionHandler(
                    currentUser,
                    writer,
                    currentAuctionId
            );

            FavouriteHandler favouriteHandler = new FavouriteHandler(
                    currentUser,
                    writer,
                    currentAuctionId
            );

            BalanceHandler balanceHandler = new BalanceHandler(
                    currentUser,
                    writer,
                    currentAuctionId
            );

            AdminHandler adminHandler = new AdminHandler(
                    currentUser,
                    writer,
                    currentAuctionId
            );

            CommandDispatcher dispatcher = new CommandDispatcher(
                    authHandler,
                    auctionHandler,
                    favouriteHandler,
                    balanceHandler,
                    adminHandler
            );

            String response = dispatcher.dispatch(cmd, data);

            this.currentUser = authHandler.getCurrentUser();
            this.currentAuctionId = auctionHandler.getCurrentAuctionId();

            return response;

        } catch (IllegalArgumentException e) {
            return "ERROR|Invalid command";
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
            return "ERROR|" + e.getMessage();
        }
    }

    private void closeConnection() {

        try {

            if (currentAuctionId != null) {
                RoomManager.removeClient(currentAuctionId, writer);
            }

            if (reader != null)
                reader.close();

            if (writer != null)
                writer.close();

            if (socket != null)
                socket.close();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
        }
    }
}
