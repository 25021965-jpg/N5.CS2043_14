package server.manager;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoomManager {
    private static final ConcurrentHashMap<String, List<PrintWriter>> rooms = new ConcurrentHashMap<>();

    public static void addClient(String auctionId, PrintWriter writer) {
        rooms.computeIfAbsent(auctionId, k -> new CopyOnWriteArrayList<>()).add(writer);
        System.out.println("[RoomManager] Client joined room: " + auctionId);
    }

    public static void removeClient(String auctionId, PrintWriter writer) {
        List<PrintWriter> room = rooms.get(auctionId);
        if (room != null) {
            room.remove(writer);
            System.out.println("[RoomManager] Client left room: " + auctionId);
            if (room.isEmpty()) {
                rooms.remove(auctionId);
                System.out.println("[RoomManager] Room deleted: " + auctionId);
            }
        }
    }

    public static void broadcastToRoom(String auctionId, String message, PrintWriter excludeWriter) {
        List<PrintWriter> room = rooms.get(auctionId);
        if (room != null) {
            for (PrintWriter writer : room) {
                if (writer != excludeWriter) {
                    writer.println(message);
                }
            }
            System.out.println("[RoomManager] Broadcast to room: " + auctionId);
        }
    }

    public static void broadcastToRoomAll(String auctionId, String message) {
        List<PrintWriter> room = rooms.get(auctionId);
        if (room != null) {
            for (PrintWriter writer : room) {
                writer.println(message);
            }
            System.out.println("[RoomManager] Broadcast to ALL in room: " + auctionId);
        }
    }
}