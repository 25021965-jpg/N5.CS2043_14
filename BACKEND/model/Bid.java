package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Bid implements Serializable {
    private String id;
    private BigDecimal amount;
    private LocalDateTime timestamp;

    private User bidder;
    private Auction auction;

    public boolean isHigherThan(Bid other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    // Getter & Setter
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public void setBidder(User bidder) { this.bidder = bidder; }
    public void setAuction(Auction auction) { this.auction = auction; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}