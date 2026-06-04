package server.manager;

import server.observer.AuctionConnectionObserver;
import server.observer.AuctionObserver;
import server.observer.AuctionSubject;

import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Quản lý các phòng đấu giá theo Observer Pattern.
 *
 * <p><b>Kiến trúc:</b>
 * <pre>
 *   RoomManager (Subject / Singleton)
 *       └─ Map<auctionId, Set<AuctionObserver>>
 *              └─ AuctionConnectionObserver
 *                       └─ writer.println(message)
 * </pre>
 *
 * <p>RoomManager đóng vai trò Subject, quản lý danh sách observer
 * của từng phòng đấu giá và broadcast sự kiện tới các client đang
 * tham gia phòng đó.
 */
public class RoomManager implements AuctionSubject {

    private static final Logger LOGGER =
            Logger.getLogger(RoomManager.class.getName());

    private static final RoomManager INSTANCE =
            new RoomManager();

    private RoomManager() {
    }

    public static RoomManager getInstance() {
        return INSTANCE;
    }

    private final ConcurrentHashMap<
            String,
            Set<AuctionObserver>
            > rooms = new ConcurrentHashMap<>();

    @Override
    public void addObserver(
            String auctionId,
            AuctionObserver observer
    ) {

        rooms.computeIfAbsent(
                auctionId,
                k -> ConcurrentHashMap.newKeySet()
        ).add(observer);

        LOGGER.info(
                "[RoomManager] observer joined "
                        + auctionId
        );
    }

    @Override
    public void removeObserver(
            String auctionId,
            AuctionObserver observer
    ) {

        Set<AuctionObserver> room =
                rooms.get(auctionId);

        if (room == null) {
            return;
        }

        room.remove(observer);

        if (room.isEmpty()) {
            rooms.remove(auctionId);
        }
    }

    @Override
    public void notifyObservers(
            String auctionId,
            String message
    ) {

        Set<AuctionObserver> room =
                rooms.get(auctionId);

        if (room == null) {
            return;
        }

        for (AuctionObserver observer : room) {

            try {

                observer.onAuctionEvent(
                        auctionId,
                        message
                );

            } catch (Exception ex) {

                LOGGER.warning(
                        "Observer error: "
                                + ex.getMessage()
                );
            }
        }
    }

    public static void broadcastToRoomAll(
            String auctionId,
            String message
    ) {

        INSTANCE.notifyObservers(
                auctionId,
                message
        );
    }
    public static void addClient(
            String auctionId,
            PrintWriter writer
    ) {

        INSTANCE.addObserver(
                auctionId,
                new AuctionConnectionObserver(writer)
        );
    }

    public static void removeClient(
            String auctionId,
            PrintWriter writer
    ) {

        Set<AuctionObserver> room =
                INSTANCE.rooms.get(auctionId);

        if (room == null) {
            return;
        }

        room.remove(
                new AuctionConnectionObserver(writer)
        );

        // Nếu client cuối cùng rời phòng thì dọn luôn room
        // tránh giữ room rỗng trong memory.
        if (room.isEmpty()) {
            INSTANCE.rooms.remove(auctionId);
        }
    }

    public static void removeClientFromAllRooms(
            PrintWriter writer
    ) {

        for (String auctionId : INSTANCE.rooms.keySet()) {

            removeClient(
                    auctionId,
                    writer
            );
        }
    }
}