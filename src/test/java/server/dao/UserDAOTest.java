package server.dao;

import model.Role;
import model.User;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest extends TiDBRollbackBase {

    @Test
    void testUserFullLifecycle() throws Exception {
        String username = "user" + System.currentTimeMillis() % 1000;
        String email = username + "@test.com";

        // 1. Phủ register
        assertTrue(UserDAO.register("Full Name", username, email, "pass123", "2000-01-01"));

        User user = UserDAO.findByUsernameOrEmail(username);
        assertNotNull(user);
        createdUserIds.add(user.getUser_id());

        // 2. Phủ login
        assertTrue(UserDAO.login(username, "pass123"));
        assertTrue(UserDAO.login(email, "pass123"));
        assertFalse(UserDAO.login(username, "wrongpass"));

        // 3. Phủ updateBalance
        assertTrue(UserDAO.updateBalance(user.getUser_id(), new BigDecimal("500.50")));

        // 4. Phủ getUserById
        assertNotNull(UserDAO.getUserById(user.getUser_id()));

        // 5. Phủ resetPassword
        assertTrue(UserDAO.resetPassword("Full Name", "2000-01-01", username, email, "newpass"));

        // 6. Phủ updatePassword & updateRole
        assertTrue(UserDAO.updatePassword(user.getUser_id(), "finalpass"));
        assertTrue(UserDAO.updateUserRole(user.getUser_id(), "ADMIN"));
        new UserDAO().updateRole(user.getUser_id(), "SELLER");

        // 7. Phủ save (ON DUPLICATE KEY)
        user.setFullname("Updated Name");
        user.setBalance(new BigDecimal("999"));
        user.setRole(Role.BIDDER);
        UserDAO.save(user);

        // 8. Phủ findAll
        List<User> all = UserDAO.findAll();
        assertFalse(all.isEmpty());

        // 9. Phủ deleteUser
        assertTrue(UserDAO.deleteUser(user.getUser_id()));
    }

    @Test
    void testUserEdgeCases() throws Exception {
        // Case: dob null
        String uId1 = generateId("unull");
        assertTrue(UserDAO.register("No Dob User", uId1, uId1 + "@null.com", "123", null));
        User u1 = UserDAO.findByUsernameOrEmail(uId1);
        createdUserIds.add(u1.getUser_id());

        // Case: save với dob empty
        User userObj = new User();
        String uId2 = generateId("usave");
        userObj.setUser_id(uId2);
        userObj.setFullname("Save Logic");
        userObj.setUsername("save_" + uId2);
        userObj.setEmail(uId2 + "@save.com");
        userObj.setPassword("123");
        userObj.setRole(Role.BIDDER);
        userObj.setBalance(BigDecimal.ZERO);
        userObj.setDob("");
        UserDAO.save(userObj);
        createdUserIds.add(uId2);

        // Case: resetPassword sai info
        assertFalse(UserDAO.resetPassword("Wrong", "1990-01-01", "w", "w@w.com", "p"));

        // Case: ID không tồn tại
        assertNull(UserDAO.getUserById("ghost"));
        assertFalse(UserDAO.deleteUser("ghost"));

        // Case: Phủ nhánh catch/null của map() bằng cách set Role = NULL trong DB
        // Cách này an toàn vì NULL luôn hợp lệ với ENUM nếu cột cho phép hoặc ta handle được
        try (var ps = conn.prepareStatement("UPDATE users SET role = NULL WHERE user_id = ?")) {
            ps.setString(1, u1.getUser_id());
            ps.executeUpdate();
        } catch (Exception ignored) {}

        User checkRole = UserDAO.getUserById(u1.getUser_id());
        assertEquals(Role.BIDDER, checkRole.getRole());
    }
}