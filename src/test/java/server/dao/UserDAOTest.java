package server.dao;

import model.Entity.User.Bidder;
import model.Entity.User.Role;
import model.Entity.User.User;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest extends TiDBRollbackBase {

    // Helper: Giả lập việc băm mật khẩu như AuthService sẽ làm
    private String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    @Test
    void testUserFullLifecycle() throws Exception {
        String username = "user" + System.currentTimeMillis() % 1000;
        String email = username + "@test.com";
        String rawPass = "pass123";

        // 1. Register: Phải dùng mật khẩu đã hash
        assertTrue(UserDAO.register("Full Name", username, email, hash(rawPass), "2000-01-01"));

        User user = UserDAO.findByUsernameOrEmail(username);
        assertNotNull(user);

        // 2. Login: DAO sẽ tự check bằng BCrypt.checkpw, nên truyền rawPass
        assertTrue(UserDAO.login(username, rawPass));
        assertTrue(UserDAO.login(email, rawPass));
        assertFalse(UserDAO.login(username, "wrongpass"));

        // 3. UpdateBalance
        assertTrue(UserDAO.updateBalance(user.getUser_id(), new BigDecimal("500.50")));

        // 4. GetUserById
        assertNotNull(UserDAO.getUserById(user.getUser_id()));

        // 5. ResetPassword: Gọi DAO resetPassword (lưu ý hàm này trong DAO đã được sửa để nhận password mới)
        String newRawPass = "newpass";
        assertTrue(UserDAO.resetPassword("Full Name", "2000-01-01", username, email, hash(newRawPass)));

        // 6. UpdatePassword và UpdateRole: Phải hash trước khi gọi
        assertTrue(UserDAO.updatePassword(user.getUser_id(), hash("finalpass")));
        assertTrue(UserDAO.updateUserRole(user.getUser_id(), "ADMIN"));
        new UserDAO().updateRole(user.getUser_id(), "SELLER");

        // 7. Save (ON DUPLICATE KEY)
        user.setFullname("Updated Name");
        user.setPassword(hash("finalpass")); // Lưu ý hash lại password nếu update đối tượng user
        user.setBalance(new BigDecimal("999"));
        user.setRole(Role.BIDDER);
        UserDAO.save(user);

        // 8. FindAll
        List<User> all = UserDAO.findAll();
        assertFalse(all.isEmpty());

        // 9. DeleteUser
        assertTrue(UserDAO.deleteUser(user.getUser_id()));
    }

    @Test
    void testUserEdgeCases() throws Exception {
        String uId1 = generateId("unull");
        assertTrue(UserDAO.register("No Dob User", uId1, uId1 + "@null.com", hash("123"), null));
        User u1 = UserDAO.findByUsernameOrEmail(uId1);
        assertNotNull(u1);

        User userObj = new Bidder();
        String uId2 = generateId("usave");
        userObj.setUser_id(uId2);
        userObj.setFullname("Save Logic");
        userObj.setUsername("save_" + uId2);
        userObj.setEmail(uId2 + "@save.com");
        userObj.setPassword(hash("123"));
        userObj.setRole(Role.BIDDER);
        userObj.setBalance(BigDecimal.ZERO);
        userObj.setDob("");
        UserDAO.save(userObj);

        assertFalse(UserDAO.resetPassword("Wrong", "1990-01-01", "w", "w@w.com", hash("p")));
        assertNull(UserDAO.getUserById("ghost"));
        assertFalse(UserDAO.deleteUser("ghost"));

        try (var ps = conn.prepareStatement("UPDATE users SET role = NULL WHERE user_id = ?")) {
            ps.setString(1, u1.getUser_id());
            ps.executeUpdate();
        } catch (Exception ignored) {}

        User checkRole = UserDAO.getUserById(u1.getUser_id());
        assertEquals(Role.BIDDER, checkRole.getRole());
    }
}