package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Auction implements Serializable {

    private String auction_id;

    private String seller_Id;

    private Item item;

    private User seller;

    private BigDecimal startingPrice;

    private BigDecimal currentPrice;

    private BigDecimal minIncrement;

    private BigDecimal floorPrice;

    public BigDecimal getFloorPrice() { return floorPrice; }
    public void setFloorPrice(BigDecimal floorPrice) { this.floorPrice = floorPrice; }

    private AuctionStatus status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private boolean is_cancelled;

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

        if (bids.isEmpty()) {
            return null;
        }

        return bids.get(bids.size() - 1);
    }

    public String getAuction_id() {
        return auction_id;
    }

    public void setAuction_id(String auction_id) {
        this.auction_id = auction_id;
    }

    public String getSeller_Id() {
        return seller_Id;
    }

    public void setSeller_Id(String seller_Id) {
        this.seller_Id = seller_Id;
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

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean isCancelled() {
        return is_cancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.is_cancelled = isCancelled;
    }

    public AuctionStatus getStatus(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        if (auction.isCancelled()) {
            return AuctionStatus.CANCELLED;
        }
        if (now.isBefore(auction.getStartTime())) {
            return AuctionStatus.UPCOMING;
        }
        if (now.isAfter(auction.getEndTime())) {
            return AuctionStatus.ENDED;
        }
        return AuctionStatus.ACTIVE;
    }


}
