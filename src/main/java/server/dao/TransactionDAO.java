package server.dao;

import model.Transaction;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionDAO {

    // ==================== THÊM GIAO DỊCH ====================
    public static boolean addTransaction(String userId, BigDecimal amount, String type) {
        String sql = "INSERT INTO transactions (transaction_id, user_id, amount, type) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, userId);
            ps.setBigDecimal(3, amount);
            ps.setString(4, type);

            int rows = ps.executeUpdate();
            System.out.println("[TransactionDAO] addTransaction rows: " + rows);
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[TransactionDAO] addTransaction error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ==================== LẤY DANH SÁCH GIAO DỊCH CỦA USER ====================
    public static List<Transaction> getTransactionsByUserId(String userId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY created_at DESC";

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
                transactions.add(tx);
            }

            System.out.println("[TransactionDAO] Loaded " + transactions.size() + " transactions for user: " + userId);

        } catch (SQLException e) {
            System.err.println("[TransactionDAO] getTransactions error: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }
}