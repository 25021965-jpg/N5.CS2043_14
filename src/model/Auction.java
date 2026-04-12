package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Auction {
    private String id;
    private User seller;
    private Item item;

    private BigDecimal startPrice;
    private BigDecimal currentPrice;
    private BigDecimal minIncrement;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private AuctionStatus status;

    // Getter & Setter
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public BigDecimal getMinIncrement() { return minIncrement; }
    public void setMinIncrement(BigDecimal minIncrement) { this.minIncrement = minIncrement; }

    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }

    public void setSeller(User seller) { this.seller = seller; }
    public void setItem(Item item) { this.item = item; }
}