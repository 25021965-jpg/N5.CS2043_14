package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Bid {

    private User bidder;
    private BigDecimal amount;
    private LocalDateTime time;
    private String username;   // THÊM: tên người đặt
    private String status;     // THÊM: LEADING / OUTBID

    public Bid(User bidder, BigDecimal amount) {
        this.bidder = bidder;
        this.amount = amount;
        this.time = LocalDateTime.now();
        this.username = bidder != null ? bidder.getUsername() : "";
        this.status = "OUTBID";
    }

    public Bid() {}

    // ==================== GETTERS ====================
    public User getBidder() { return bidder; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getTime() { return time; }
    public String getUsername() { return username; }
    public String getStatus() { return status; }

    // ==================== SETTERS ====================
    public void setBidder(User bidder) { this.bidder = bidder; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setTime(LocalDateTime time) { this.time = time; }
    public void setUsername(String username) { this.username = username; }
    public void setStatus(String status) { this.status = status; }

    // ==================== FOR TABLEVIEW ====================
    public String getTimeString() {
        return time != null ? time.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "";
    }

    public void setTimeString(String timeStr) {
        try {
            this.time = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
        } catch (Exception e) {
            this.time = LocalDateTime.now();
        }
    }

    public String getAmountString() {
        if (amount == null) return "0 ₫";
        return String.format("%,.0f ₫", amount);
    }

    public void setAmountString(String amountStr) {
        try {
            String clean = amountStr.replace("₫", "").replace(".", "").trim();
            this.amount = new BigDecimal(clean);
        } catch (Exception e) {
            this.amount = BigDecimal.ZERO;
        }
    }
}