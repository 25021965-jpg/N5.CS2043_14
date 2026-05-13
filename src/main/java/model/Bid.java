package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Bid  {

    private User bidder;
    private BigDecimal amount;
    private LocalDateTime time;

    public Bid(User bidder, BigDecimal amount) {
        this.bidder = bidder;
        this.amount = amount;
        this.time = LocalDateTime.now();
    }

    public Bid() {}

    public User getBidder() { return bidder; }
    public void setBidder(User bidder) { this.bidder = bidder; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }
}