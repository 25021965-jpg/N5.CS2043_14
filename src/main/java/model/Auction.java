package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class Auction implements Serializable {

    private String id;
    private Item item;
    private User seller;

    private BigDecimal currentPrice;
    private BigDecimal minIncrement;

    private AuctionStatus status;

    private LocalDateTime endTime;

    private List<Bid> bids = new ArrayList<>();

    public Auction() {
    }

    public void addBid(Bid bid) {

        if (bid == null) {
            return;
        }

        bids.add(bid);
        currentPrice = bid.getAmount();
    }

    public Bid getHighestBid() {
        if (bids.isEmpty()) return null;
        return bids.get(bids.size() - 1);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getMinIncrement() {
        return minIncrement;
    }

    public void setMinIncrement(BigDecimal minIncrement) {
        this.minIncrement = minIncrement;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public List<Bid> getBids() {
        return bids;
    }

    public void setBids(List<Bid> bids) {
        this.bids = bids;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

}