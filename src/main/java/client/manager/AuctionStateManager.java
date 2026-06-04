package client.manager;

import model.AuctionUserState;
import model.ParticipationStatus;
import model.PaymentStatus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AuctionStateManager {
    private static final Map<String, AuctionUserState> stateMap =
            new ConcurrentHashMap<>();

    private static final Map<String, List<AuctionStateListener>> listeners =
            new ConcurrentHashMap<>();

    private AuctionStateManager() {}

    // ==================== CORE STATE ====================
    public static AuctionUserState get(String auctionId) {
        return stateMap.computeIfAbsent(
                auctionId,
                AuctionUserState::new
        );
    }

    // ==================== PARTICIPATION ====================
    public static void setParticipation(String auctionId, ParticipationStatus status) {
        AuctionUserState state = get(auctionId);
        // Không override WON/LOST
        ParticipationStatus current = state.getParticipationStatus();
        if (current == ParticipationStatus.WON
                || current == ParticipationStatus.LOST) {
            return;
        }

        if (current == status) return;

        if (state.getParticipationStatus() == status) {
            return;
        }
        System.out.println(
                "[STATE] "
                        + auctionId
                        + " -> "
                        + status
        );

        state.setParticipationStatus(status);
        notifyChange(auctionId);
    }

    public static ParticipationStatus getParticipation(String auctionId) {
        return get(auctionId).getParticipationStatus();
    }

    public static boolean isJoined(String auctionId) {
        ParticipationStatus status =
                getParticipation(auctionId);

        return status != ParticipationStatus.NOT_JOINED;
    }

    public static void join(String auctionId) {
        setParticipation(auctionId, ParticipationStatus.JOINED);
    }

    public static void leave(String auctionId) {
        setParticipation(auctionId, ParticipationStatus.NOT_JOINED);
    }

    // ==================== PAYMENT ====================
    public static void setPayment(String auctionId, PaymentStatus status) {
        AuctionUserState state = get(auctionId);

        if (state.getPaymentStatus() == status) {
            return;
        }
        state.setPaymentStatus(status);
        notifyChange(auctionId);
    }

    public static PaymentStatus getPayment(String auctionId) {
        return get(auctionId).getPaymentStatus();
    }

    // ==================== LISTENER ====================
    public static void registerListener(
            String auctionId,
            AuctionStateListener listener
    ) {
        List<AuctionStateListener> list =
                listeners.computeIfAbsent(
                        auctionId,
                        k -> new CopyOnWriteArrayList<>()
                );

        if (!list.contains(listener)) {
            list.add(listener);
        }
    }

    public static void unregisterListener(
            String auctionId,
            AuctionStateListener listener
    ) {
        List<AuctionStateListener> list =
                listeners.get(auctionId);

        if (list == null) {
            return;
        }

        list.remove(listener);

        if (list.isEmpty()) {
            listeners.remove(auctionId);
        }
    }

    private static void notifyChange(String auctionId) {

        List<AuctionStateListener> list =
                listeners.get(auctionId);

        if (list == null) {
            return;
        }

        for (AuctionStateListener listener : list) {
            listener.onAuctionStateChanged(auctionId);
        }
    }

    // ==================== SAFE CLEANUP ====================

    /**
     * Chỉ clear 1 auction cụ thể (an toàn)
     */
    public static void clear(String auctionId) {
        stateMap.remove(auctionId);
    }

    /**
     * Clear toàn bộ (chỉ dùng khi logout/app restart)
     */
    public static void clearAll() {
        stateMap.clear();
        listeners.clear();
    }
}