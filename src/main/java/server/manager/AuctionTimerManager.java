package server.manager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Quản lý Timer kết thúc auction — có thể hủy và đặt lại khi anti-snipe.
 */
public class AuctionTimerManager {

    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(10);
    // auctionId → Future của timer hiện tại
    private static final ConcurrentHashMap<String, ScheduledFuture<?>> timers =
            new ConcurrentHashMap<>();

    /**
     * Đặt (hoặc đặt lại) timer kết thúc auction.
     * Nếu đã có timer cũ → hủy trước, rồi tạo mới.
     */
    public static void scheduleEnd(String auctionId, long delayMillis, Runnable task) {
        // Hủy timer cũ nếu có
        ScheduledFuture<?> existing = timers.get(auctionId);
        if (existing != null && !existing.isDone()) {
            existing.cancel(false);
            System.out.println("[AuctionTimerManager] Cancelled old timer for: " + auctionId);
        }

        // Tạo timer mới
        ScheduledFuture<?> future = scheduler.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
        timers.put(auctionId, future);
        System.out.println("[AuctionTimerManager] Scheduled end for: " + auctionId
                + " in " + (delayMillis / 1000) + "s");
    }

    public static void cancelTimer(String auctionId) {
        ScheduledFuture<?> future = timers.remove(auctionId);
        if (future != null) future.cancel(false);
    }
}