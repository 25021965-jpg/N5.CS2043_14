package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Bid {

    private User bidder;
    private BigDecimal amount;
    private LocalDateTime time;
    private String username;
    private String status;
    private String amountString;
    private String timeString;

    public Bid(User bidder, BigDecimal amount) {
        this.bidder = bidder;
        this.amount = amount;
        this.time = LocalDateTime.now();
        this.username = bidder != null ? bidder.getUsername() : "";
        this.status = "OUTBID";
        this.amountString = formatAmount(amount);
    }

    public Bid() {}

    // ==================== GETTERS ====================
    public User getBidder() { return bidder; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getTime() { return time; }
    public String getUsername() { return username; }
    public String getStatus() { return status; }
    public String getAmountString() { return amountString; }

    // ==================== SETTERS ====================
    public void setBidder(User bidder) { this.bidder = bidder; }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
        this.amountString = formatAmount(amount);
    }

    public void setTime(LocalDateTime time) { this.time = time; }
    public void setUsername(String username) { this.username = username; }
    public void setStatus(String status) { this.status = status; }
    public void setAmountString(String amountString) { this.amountString = amountString; }

    // ==================== FOR TABLEVIEW ====================
    public String getTimeString() {
        if (timeString != null && !timeString.isEmpty()) {
            return timeString;
        }
        return time != null ? time.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "";
    }

    public void setTimeString(String timeStr) {
        this.timeString = timeStr;
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0 USD";
        return String.format("%,.0f USD", amount);
    }
}