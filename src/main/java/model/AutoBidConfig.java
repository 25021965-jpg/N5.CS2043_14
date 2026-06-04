package model;

import java.math.BigDecimal;

public class AutoBidConfig {
    private String userId;
    private String auctionId;
    private BigDecimal maxAmount;   // Giá tối đa người dùng chấp nhận
    private boolean active;
    private final long registeredAt = System.currentTimeMillis(); ;


    public AutoBidConfig(String userId, String auctionId, BigDecimal maxAmount) {
        this.userId = userId;
        this.auctionId = auctionId;
        this.maxAmount = maxAmount;
        this.active = true;
    }

    // Getters & Setters
    public String getUserId() { return userId; }
    public String getAuctionId() { return auctionId; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public long getRegisteredAt() { return registeredAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}