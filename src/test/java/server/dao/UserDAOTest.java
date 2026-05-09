package server.dao;

import model.User;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {

    private static final String PREFIX = "TEST_";
    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
    }

    // Hàm hỗ trợ tạo User chuẩn, tránh lỗi thiếu trường (NOT NULL) ở mọi Test Case
    private User createTestUser(String suffix) {
        User u = new User();
        u.setId(PREFIX + "ID_" + suffix);
        u.setFullname("Test User " + suffix);
        u.setUsername(PREFIX + "U_" + suffix);
        u.setEmail(PREFIX + "E_" + suffix + "@test.com");
        u.setPassword("password123");
        return u;
    }

    @AfterEach
    void cleanUp() {
        // Xóa theo ID, Username hoặc Email có chứa PREFIX
        String sql = "DELETE FROM users WHERE username LIKE ? OR id LIKE ? OR email LIKE ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = PREFIX + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("CleanUp Error: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test 1: Full Cycle - Save, Find, and Login")
    void testFullUserCycle() {
        User u = createTestUser(UUID.randomUUID().toString().substring(0, 5));

        // 1. Test Save
        assertDoesNotThrow(() -> userDAO.save(u));

        // 2. Test findByUsername
        User foundByNick = userDAO.findByUsername(u.getUsername());
        assertNotNull(foundByNick, "Should find user by username after saving");
        assertEquals(u.getEmail(), foundByNick.getEmail());

        // 3. Test findByEmail
        User foundByMail = userDAO.findByEmail(u.getEmail());
        assertNotNull(foundByMail, "Should find user by email after saving");

        // 4. Test login (Success & Failure)
        assertTrue(UserDAO.login(u.getUsername(), "password123"), "Login should succeed");
        assertFalse(UserDAO.login(u.getUsername(), "wrong_pass"), "Login should fail with wrong password");
    }

    @Test
    @DisplayName("Test 2: Find All Users")
    void testFindAll() {
        // Luôn tạo user đầy đủ thông tin để không bị lỗi NOT NULL
        User u = createTestUser("LIST_" + UUID.randomUUID().toString().substring(0, 3));
        userDAO.save(u);

        List<User> list = userDAO.findAll();
        assertNotNull(list);
        assertFalse(list.isEmpty(), "User list should not be empty after a save");

        // Kiểm tra xem user vừa tạo có nằm trong list không
        boolean found = list.stream().anyMatch(user -> user.getUsername().equals(u.getUsername()));
        assertTrue(found, "The saved user must be present in findAll results");
    }

    @Test
    @DisplayName("Test 3: Negative Cases (Data not found)")
    void testNegativeCases() {
        String fakeKey = "NON_EXISTENT_" + UUID.randomUUID();

        assertNull(userDAO.findByUsername(fakeKey));
        assertNull(userDAO.findByEmail(fakeKey + "@test.com"));
        assertNull(userDAO.findByUsernameOrEmail(fakeKey));

        assertFalse(UserDAO.login("fake_user_abc", "fake_pass"));
    }

    @Test
    @DisplayName("Test 4: Logic checking in findByUsernameOrEmail")
    void testUsernameOrEmailLogic() {
        User u = createTestUser("LOGIC_" + UUID.randomUUID().toString().substring(0, 5));

        assertDoesNotThrow(() -> userDAO.save(u));

        // Test tìm kiếm bằng Username
        User byUser = userDAO.findByUsernameOrEmail(u.getUsername());
        assertNotNull(byUser, "Hàm findByUsernameOrEmail trả về null khi tìm bằng Username");

        // Test tìm kiếm bằng Email
        User byEmail = userDAO.findByUsernameOrEmail(u.getEmail());
        assertNotNull(byEmail, "Hàm findByUsernameOrEmail trả về null khi tìm bằng Email");

        assertEquals(byUser.getId(), byEmail.getId(), "Both searches must return the same user ID");
    }
}