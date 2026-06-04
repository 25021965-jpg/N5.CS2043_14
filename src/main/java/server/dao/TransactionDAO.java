package server.dao;

import model.Transaction;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionDAO {
    private static final Logger LOGGER =
            Logger.getLogger(TransactionDAO.class.getName());

    // ==================== THÊM GIAO DỊCH ====================
    public static boolean addTransaction(
            String userId,
            String relatedUserId,
            BigDecimal amount,
            String type,
            String description
    )    {
        String sql = """
INSERT INTO transactions
(transaction_id, user_id, related_user_id, amount, type, description)
VALUES (?, ?, ?, ?, ?, ?)
""";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, userId);
            if (relatedUserId == null) {ps.setNull(3, Types.CHAR);}
            else {ps.setString(3, relatedUserId);}
            ps.setBigDecimal(4, amount);
            ps.setString(5, type);
            ps.setString(6, description == null ? "" : description);
            int rows = ps.executeUpdate();
            System.out.println("[TransactionDAO] addTransaction rows: " + rows);
            return rows > 0;

        } catch (SQLException e) {
            LOGGER.severe("[TransactionDAO] addTransaction error: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
            return false;
        }
    }

    // ==================== LẤY DANH SÁCH GIAO DỊCH CỦA USER ====================
    public static List<Transaction> getTransactionsByUserId(String userId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = """
                SELECT t.*, u.username AS related_username
                FROM transactions t
                LEFT JOIN users u
                ON t.related_user_id = u.user_id
                WHERE t.user_id = ?
                ORDER BY t.created_at DESC""";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Transaction tx = new Transaction();
                tx.setTransactionId(rs.getString("transaction_id"));
                tx.setUserId(rs.getString("user_id"));
                tx.setAmount(rs.getBigDecimal("amount"));
                tx.setType(rs.getString("type"));
                tx.setCreatedAt(rs.getTimestamp("created_at"));
                tx.setDescription(rs.getString("description"));
                tx.setRelatedUserId(rs.getString("related_user_id"));
                tx.setRelatedUsername(rs.getString("related_username"));
                transactions.add(tx);
            }

            System.out.println("[TransactionDAO] Loaded " + transactions.size() + " transactions for user: " + userId);

        } catch (SQLException e) {
            LOGGER.severe("[TransactionDAO] getTransactions error: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
        }
        return transactions;
    }

    public static boolean addTransaction(String userId, BigDecimal amount, String type) {
        return addTransaction(userId, null, amount, type, "");
    }
}