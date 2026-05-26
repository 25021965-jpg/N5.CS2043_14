package model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class Transaction {

    private String transactionId, userId, relatedUserId, relatedUsername, type, description;
    private BigDecimal amount;
    private Timestamp createdAt;

    public Transaction() {}

    public String getTransactionId() { return transactionId; }
    public String getUserId() { return userId; }
    public String getRelatedUserId() { return relatedUserId; }
    public String getRelatedUsername() { return relatedUsername; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public Timestamp getCreatedAt() { return createdAt; }

    public void setTransactionId(String v) { transactionId = v; }
    public void setUserId(String v) { userId = v; }
    public void setRelatedUserId(String v) { relatedUserId = v; }
    public void setRelatedUsername(String v) { relatedUsername = v; }
    public void setType(String v) { type = v; }
    public void setDescription(String v) { description = v; }
    public void setAmount(BigDecimal v) { amount = v; }
    public void setCreatedAt(Timestamp v) { createdAt = v; }

    public String getFormattedTime() {
        return createdAt == null ? "" :
                createdAt.toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"));
    }

    public String getDisplayTitle() {
        return switch (type) {
            case "DEPOSIT" -> "Deposit Successfully";
            case "WITHDRAW" -> "Withdraw Successfully";
            case "TRANSFER_OUT" -> "Paid to " + relatedUsername;
            case "TRANSFER_IN" -> "Received from " + relatedUsername;
            default -> type;
        };
    }

    public boolean isPositive() {
        return "DEPOSIT".equals(type) || "TRANSFER_IN".equals(type);
    }

    public String getFormattedAmount() {
        return (isPositive() ? "+ " : "- ")
                + String.format("%,.0f", amount)
                + " USD";
    }
}