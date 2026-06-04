package server.dao;

import model.Entity.User.Role;
import model.Entity.User.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest extends TiDBRollbackBase {

    private String userId;
    private final String username = "testuser" + System.currentTimeMillis();
    private final String email = username + "@test.com";
    private final String rawPass = "password123";
    private final String hashedPass = BCrypt.hashpw(rawPass, BCrypt.gensalt(12));

    @BeforeEach
    void initData() throws Exception {
        // Đăng ký user để test các hàm khác
        UserDAO.register("Full Name", username, email, hashedPass, "2000-01-01");
        User user = UserDAO.findByUsernameOrEmail(username);
        assertNotNull(user);
        userId = user.getUser_id();
    }

    // 1. Test Authentication & Registration
    @Test
    void testAuth() {
        assertTrue(UserDAO.login(username, rawPass));
        assertFalse(UserDAO.login(username, "wrong"));
        assertFalse(UserDAO.login("unknown", "pass"));
    }

    // 2. Test Balance & Virtual Balance (Cover 100% các nhánh atomic)
    @Test
    void testBalances() {
        // Update Balance
        assertTrue(UserDAO.updateBalance(userId, new BigDecimal("100.00")));

        BigDecimal currentBalance = UserDAO.getVirtualBalance(userId);
        assertEquals(0, BigDecimal.ZERO.compareTo(currentBalance), "Balance should be ZERO");

        assertTrue(UserDAO.initVirtualBalance(userId, new BigDecimal("500.00")));
        assertTrue(UserDAO.addVirtualBalance(userId, new BigDecimal("100.00")));

        // Deduct
        assertTrue(UserDAO.deductVirtualBalance(userId, new BigDecimal("200.00")));
        assertFalse(UserDAO.deductVirtualBalance(userId, new BigDecimal("9999.00")));
    }

    // 3. Test Reset Password & Update Role/Pass
    @Test
    void testUpdates() {
        // Reset password
        assertTrue(UserDAO.resetPassword("Full Name", "2000-01-01", username, email, BCrypt.hashpw("new", BCrypt.gensalt(12))));

        // Update role
        assertTrue(UserDAO.updateUserRole(userId, "ADMIN"));
        new UserDAO().updateRole(userId, "BIDDER");

        // Update password
        assertTrue(UserDAO.updatePassword(userId, hashedPass));
    }

    // 4. Test Save (ON DUPLICATE KEY)
    @Test
    void testSave() {
        User user = UserDAO.getUserById(userId);
        user.setFullname("Updated Name");
        UserDAO.save(user); // Thực hiện update thông qua save

        assertEquals("Updated Name", UserDAO.getUserById(userId).getFullname());
    }

    // 5. Test Edge Cases & Deletion
    @Test
    void testEdgeCases() {
        // Dob null/empty
        assertTrue(UserDAO.register("NoDob", "nodob", "no@dob.com", hashedPass, null));

        // FindAll
        List<User> users = UserDAO.findAll();
        assertFalse(users.isEmpty());

        // Delete
        assertTrue(UserDAO.deleteUser(userId));
        assertFalse(UserDAO.deleteUser("non-existent-id")); // Cover catch block hoặc return false
    }
}