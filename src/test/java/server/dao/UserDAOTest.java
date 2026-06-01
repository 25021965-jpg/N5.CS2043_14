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
        // Dùng timestamp để đảm bảo username không trùng khi chạy nhiều lần
        String username = "user" + System.currentTimeMillis() % 1000;
        String email = username + "@test.com";

        // 1. Phủ register — tạo tài khoản mới
        assertTrue(UserDAO.register("Full Name", username, email, "pass123", "2000-01-01"));

        User user = UserDAO.findByUsernameOrEmail(username);
        assertNotNull(user);

        // 2. Phủ login — đăng nhập bằng username và email
        assertTrue(UserDAO.login(username, "pass123"));
        assertTrue(UserDAO.login(email, "pass123"));
        assertFalse(UserDAO.login(username, "wrongpass")); // sai mật khẩu

        // 3. Phủ updateBalance — cập nhật số dư
        assertTrue(UserDAO.updateBalance(user.getUser_id(), new BigDecimal("500.50")));

        // 4. Phủ getUserById — lấy thông tin theo ID
        assertNotNull(UserDAO.getUserById(user.getUser_id()));

        // 5. Phủ resetPassword — đặt lại mật khẩu bằng thông tin cá nhân
        assertTrue(UserDAO.resetPassword("Full Name", "2000-01-01", username, email, "newpass"));

        // 6. Phủ updatePassword và updateRole — đổi mật khẩu và quyền
        assertTrue(UserDAO.updatePassword(user.getUser_id(), "finalpass"));
        assertTrue(UserDAO.updateUserRole(user.getUser_id(), "ADMIN"));
        new UserDAO().updateRole(user.getUser_id(), "SELLER");

        // 7. Phủ save (ON DUPLICATE KEY) — cập nhật thông tin user đã tồn tại
        user.setFullname("Updated Name");
        user.setBalance(new BigDecimal("999"));
        user.setRole(Role.BIDDER);
        UserDAO.save(user);

        // 8. Phủ findAll — lấy danh sách tất cả user
        List<User> all = UserDAO.findAll();
        assertFalse(all.isEmpty());

        // 9. Phủ deleteUser — xóa tài khoản
        assertTrue(UserDAO.deleteUser(user.getUser_id()));
    }

    @Test
    void testUserEdgeCases() throws Exception {
        // Trường hợp: đăng ký không có ngày sinh
        String uId1 = generateId("unull");
        assertTrue(UserDAO.register("No Dob User", uId1, uId1 + "@null.com", "123", null));
        User u1 = UserDAO.findByUsernameOrEmail(uId1);
        assertNotNull(u1);

        // Trường hợp: save user với dob là chuỗi rỗng
        User userObj = new User();
        String uId2 = generateId("usave");
        userObj.setUser_id(uId2);
        userObj.setFullname("Save Logic");
        userObj.setUsername("save_" + uId2);
        userObj.setEmail(uId2 + "@save.com");
        userObj.setPassword("123");
        userObj.setRole(Role.BIDDER);
        userObj.setBalance(BigDecimal.ZERO);
        userObj.setDob(""); // dob rỗng → không lưu ngày sinh
        UserDAO.save(userObj);

        // Trường hợp: resetPassword với thông tin sai → trả về false
        assertFalse(UserDAO.resetPassword("Wrong", "1990-01-01", "w", "w@w.com", "p"));

        // Trường hợp: ID không tồn tại → trả về null hoặc false
        assertNull(UserDAO.getUserById("ghost"));
        assertFalse(UserDAO.deleteUser("ghost"));

        // Trường hợp: set role = NULL trong DB để kiểm tra nhánh xử lý role null trong map()
        // Cách này an toàn vì NULL được handle bằng fallback về BIDDER
        try (var ps = conn.prepareStatement("UPDATE users SET role = NULL WHERE user_id = ?")) {
            ps.setString(1, u1.getUser_id());
            ps.executeUpdate();
        } catch (Exception ignored) {}

        // Khi role = NULL → phải fallback về BIDDER, không crash
        User checkRole = UserDAO.getUserById(u1.getUser_id());
        assertEquals(Role.BIDDER, checkRole.getRole());
    }
}