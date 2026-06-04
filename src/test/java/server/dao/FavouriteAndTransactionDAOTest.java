package server.dao;

import model.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FavouriteAndTransactionDAOTest extends TiDBRollbackBase {

    // ==================================================
    //  FavouriteDAO
    // ==================================================

    @Test
    void addFavourite_newEntry_returnsTrueAndPersisted() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 0);
        insertItem("item-t1", "TestWatch", "ELECTRONICS");

        // Thêm mới → trả về true và tồn tại trong DB
        assertTrue(FavouriteDAO.addFavourite("u-t1", "item-t1"));
        assertTrue(FavouriteDAO.isFavourite("u-t1", "item-t1"));
    }

    @Test
    void addFavourite_duplicate_returnsFalse() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 0);
        insertItem("item-t1", "TestWatch", "ELECTRONICS");
        FavouriteDAO.addFavourite("u-t1", "item-t1");

        // Thêm lần 2 cùng cặp → vi phạm PRIMARY KEY, trả về false
        assertFalse(FavouriteDAO.addFavourite("u-t1", "item-t1"));
    }

    @Test
    void removeFavourite_existingEntry_returnsTrueAndGone() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 0);
        insertItem("item-t1", "TestWatch", "ELECTRONICS");
        FavouriteDAO.addFavourite("u-t1", "item-t1");

        // Xóa → trả về true và không còn trong DB
        assertTrue(FavouriteDAO.removeFavourite("u-t1", "item-t1"));
        assertFalse(FavouriteDAO.isFavourite("u-t1", "item-t1"));
    }

    @Test
    void removeFavourite_nonExistent_returnsFalse() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 0);

        // Xóa item không tồn tại → không crash, trả về false
        assertFalse(FavouriteDAO.removeFavourite("u-t1", "ghost-item-xyz"));
    }

    @Test
    void isFavourite_beforeAdd_returnsFalse() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 0);
        insertItem("item-t1", "TestWatch", "ELECTRONICS");

        // Chưa thêm vào yêu thích → phải trả về false
        assertFalse(FavouriteDAO.isFavourite("u-t1", "item-t1"));
    }

    @Test
    void getFavouriteItemIds_noFavourites_returnsEmpty() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 0);

        // User chưa có item yêu thích → danh sách rỗng
        assertTrue(FavouriteDAO.getFavouriteItemIds("u-t1").isEmpty());
    }

    @Test
    void getFavouriteItemIds_multipleItems_returnsAll() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 0);
        insertItem("item-t1", "TestWatch", "ELECTRONICS");
        insertItem("item-t2", "TestRing", "JEWELRY");
        FavouriteDAO.addFavourite("u-t1", "item-t1");
        FavouriteDAO.addFavourite("u-t1", "item-t2");

        // Phải trả về đúng 2 item
        List<String> ids = FavouriteDAO.getFavouriteItemIds("u-t1");
        assertEquals(2, ids.size());
        assertTrue(ids.contains("item-t1"));
        assertTrue(ids.contains("item-t2"));
    }

    @Test
    void getFavouriteItemIds_isolatedPerUser() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 0);
        insertUser("u-t2", "tuser2", "tuser2@mail.com", "pass", "BIDDER", 0);
        insertItem("item-t1", "TestWatch", "ELECTRONICS");
        insertItem("item-t2", "TestRing", "JEWELRY");
        FavouriteDAO.addFavourite("u-t1", "item-t1");
        FavouriteDAO.addFavourite("u-t2", "item-t2");

        // Danh sách yêu thích của mỗi user phải độc lập với nhau
        List<String> user1Favs = FavouriteDAO.getFavouriteItemIds("u-t1");
        assertEquals(1, user1Favs.size());
        assertTrue(user1Favs.contains("item-t1"));
        assertFalse(user1Favs.contains("item-t2")); // item của user khác không xuất hiện
    }

    // ==================================================
    //  TransactionDAO
    // ==================================================

    @Test
    void addTransaction_deposit_returnsTrueAndPersisted() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 0);
        assertTrue(TransactionDAO.addTransaction("u-t1", new BigDecimal("500"), "DEPOSIT"));

        // Xác nhận transaction được lưu với đúng loại và số tiền
        try (var ps = conn.prepareStatement("SELECT type, amount FROM transactions WHERE user_id = ?")) {
            ps.setString(1, "u-t1");
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals("DEPOSIT", rs.getString("type"));
            assertEquals(0, new BigDecimal("500").compareTo(rs.getBigDecimal("amount")));
        }
    }

    @Test
    void addTransaction_withdraw_persisted() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 1000);

        // Giao dịch rút tiền cũng được lưu bình thường
        assertTrue(TransactionDAO.addTransaction("u-t1", new BigDecimal("200"), "WITHDRAW"));
    }

    @Test
    void addTransaction_withRelatedUser_relatedIdPersisted() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 1000);
        insertUser("u-t2", "tuser2", "tuser2@mail.com", "pass", "BIDDER", 0);

        assertTrue(TransactionDAO.addTransaction("u-t1", "u-t2", new BigDecimal("300"), "TRANSFER_OUT", "Payment"));

        // Kiểm tra related_user_id và description được lưu đúng
        try (var ps = conn.prepareStatement(
                "SELECT related_user_id, description FROM transactions WHERE user_id = ? AND type = 'TRANSFER_OUT'")) {
            ps.setString(1, "u-t1");
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals("u-t2", rs.getString("related_user_id"));
            assertEquals("Payment", rs.getString("description"));
        }
    }

    @Test
    void addTransaction_nullRelatedUser_savedAsNull() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 0);
        TransactionDAO.addTransaction("u-t1", null, new BigDecimal("100"), "DEPOSIT", null);

        // related_user_id null được lưu thành NULL trong DB
        try (var ps = conn.prepareStatement("SELECT related_user_id FROM transactions WHERE user_id = ?")) {
            ps.setString(1, "u-t1");
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            assertNull(rs.getString("related_user_id"));
        }
    }

    @Test
    void addTransaction_nullDescription_savedAsEmpty() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 0);
        TransactionDAO.addTransaction("u-t1", null, new BigDecimal("100"), "DEPOSIT", null);

        // description null được lưu thành chuỗi rỗng
        try (var ps = conn.prepareStatement("SELECT description FROM transactions WHERE user_id = ?")) {
            ps.setString(1, "u-t1");
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals("", rs.getString("description"));
        }
    }

    @Test
    void getTransactionsByUserId_noTransactions_returnsEmpty() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 0);

        // User chưa có giao dịch nào → danh sách rỗng
        assertTrue(TransactionDAO.getTransactionsByUserId("u-t1").isEmpty());
    }

    @Test
    void getTransactionsByUserId_multipleTransactions_returnsAll() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 0);
        TransactionDAO.addTransaction("u-t1", new BigDecimal("500"), "DEPOSIT");
        TransactionDAO.addTransaction("u-t1", new BigDecimal("200"), "WITHDRAW");
        TransactionDAO.addTransaction("u-t1", new BigDecimal("100"), "DEPOSIT");

        // Tất cả 3 giao dịch phải được trả về
        assertEquals(3, TransactionDAO.getTransactionsByUserId("u-t1").size());
    }

    @Test
    void getTransactionsByUserId_mappedCorrectly() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 0);
        TransactionDAO.addTransaction("u-t1", new BigDecimal("750"), "DEPOSIT");

        List<Transaction> txs = TransactionDAO.getTransactionsByUserId("u-t1");
        assertEquals(1, txs.size());

        // Kiểm tra mapping đầy đủ các field
        Transaction tx = txs.get(0);
        assertEquals("u-t1", tx.getUserId());
        assertEquals("DEPOSIT", tx.getType());
        assertEquals(0, new BigDecimal("750").compareTo(tx.getAmount()));
        assertNotNull(tx.getCreatedAt());
        assertNotNull(tx.getTransactionId()); // UUID phải được sinh ra
    }

    @Test
    void getTransactionsByUserId_onlyForSpecificUser() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 0);
        insertUser("u-t2", "tuser2", "tuser2@mail.com", "pass", "BIDDER", 0);
        TransactionDAO.addTransaction("u-t1", new BigDecimal("500"), "DEPOSIT");
        TransactionDAO.addTransaction("u-t2", new BigDecimal("300"), "DEPOSIT");

        // Chỉ trả về giao dịch của đúng user, không lẫn với user khác
        List<Transaction> txs = TransactionDAO.getTransactionsByUserId("u-t1");
        assertEquals(1, txs.size());
        assertEquals("u-t1", txs.get(0).getUserId());
    }

    @Test
    void getTransactionsByUserId_transferRelatedUsername_populated() throws Exception {
        insertUser("u-t1", "tuser1", "tuser1@mail.com", "pass", "BIDDER", 0);
        insertUser("u-t2", "tuser2", "tuser2@mail.com", "pass", "BIDDER", 0);
        TransactionDAO.addTransaction("u-t1", "u-t2", new BigDecimal("200"), "TRANSFER_OUT", "test");

        // Username của người nhận phải được JOIN và hiển thị đúng
        List<Transaction> txs = TransactionDAO.getTransactionsByUserId("u-t1");
        assertEquals("tuser2", txs.get(0).getRelatedUsername());
    }

    @Test
    void getTransactionsByUserId_nonExistentUser_returnsEmpty() {
        // User không tồn tại → không crash, trả về danh sách rỗng
        assertTrue(TransactionDAO.getTransactionsByUserId("ghost-user-xyz").isEmpty());
    }
}