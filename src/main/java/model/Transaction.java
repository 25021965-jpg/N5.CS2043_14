package model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private String transactionId;
    private String userId;
    private BigDecimal amount;
    private String type;  // DEPOSIT or WITHDRAW
    private Timestamp createdAt;

    public Transaction() {}

    // Getters
    public String getTransactionId() { return transactionId; }
    public String getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public String getType() { return type; }
    public Timestamp getCreatedAt() { return createdAt; }

    // Setters
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setType(String type) { this.type = type; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // Helper methods cho UI
    public String getFormattedTime() {
        if (createdAt == null) return "";
        return createdAt.toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"));
    }

    public String getFormattedAmount() {
        if ("DEPOSIT".equals(type)) {
            return "+ " + String.format("%,.0f", amount) + " USD";
        } else {
            return "- " + String.format("%,.0f", amount) + " USD";
        }
    }
}