package server.service;

import model.Entity.User.Bidder;
import model.Entity.User.Role;
import model.Entity.User.User;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import server.dao.UserDAO;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private User makeUser(String passwordHash) {
        User u = new Bidder();
        u.setUser_id("u1");
        u.setUsername("alice");
        u.setRole(Role.BIDDER);
        u.setPassword(passwordHash);
        return u;
    }

    @Test
    void login_validCredentials_returnsUser() {
        String password = "password123";
        User user = makeUser(org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt(12)));

        try (MockedStatic<UserDAO> userDao = Mockito.mockStatic(UserDAO.class)) {
            userDao.when(() -> UserDAO.findByUsernameOrEmail("alice"))
                    .thenReturn(user);

            User result = AuthService.login("alice", password);
            assertNotNull(result);
            assertEquals("alice", result.getUsername());
        }
    }

    @Test
    void login_invalidPassword_returnsNull() {
        User user = makeUser(org.mindrot.jbcrypt.BCrypt.hashpw("password123", org.mindrot.jbcrypt.BCrypt.gensalt(12)));

        try (MockedStatic<UserDAO> userDao = Mockito.mockStatic(UserDAO.class)) {
            userDao.when(() -> UserDAO.findByUsernameOrEmail("alice"))
                    .thenReturn(user);

            assertNull(AuthService.login("alice", "wrong-password"));
        }
    }

    @Test
    void login_unknownUser_returnsNull() {
        try (MockedStatic<UserDAO> userDao = Mockito.mockStatic(UserDAO.class)) {
            userDao.when(() -> UserDAO.findByUsernameOrEmail("missing"))
                    .thenReturn(null);

            assertNull(AuthService.login("missing", "password"));
        }
    }

    @Test
    void register_existingUsername_returnsNull() {
        User existing = makeUser("pwd");

        try (MockedStatic<UserDAO> userDao = Mockito.mockStatic(UserDAO.class)) {
            userDao.when(() -> UserDAO.findByUsernameOrEmail("alice"))
                    .thenReturn(existing);
            userDao.when(() -> UserDAO.findByUsernameOrEmail("alice@example.com"))
                    .thenReturn(existing);

            assertNull(AuthService.register("Alice", "alice", "alice@example.com", "password123", "1990-01-01"));
        }
    }

    @Test
    void register_newUser_succeeds() {
        User saved = makeUser("pwdhash");

        try (MockedStatic<UserDAO> userDao = Mockito.mockStatic(UserDAO.class)) {
            userDao.when(() -> UserDAO.findByUsernameOrEmail("alice"))
                    .thenReturn(null, saved);
            userDao.when(() -> UserDAO.findByUsernameOrEmail("alice@example.com"))
                    .thenReturn(null);
            userDao.when(() -> UserDAO.register(
                            Mockito.anyString(),
                            Mockito.eq("alice"),
                            Mockito.eq("alice@example.com"),
                            Mockito.anyString(),
                            Mockito.eq("1990-01-01")))
                    .thenReturn(true);

            User result = AuthService.register("Alice", "alice", "alice@example.com", "password123", "1990-01-01");
            assertNotNull(result);
            assertEquals("alice", result.getUsername());
        }
    }

    @Test
    void resetPassword_success_returnsTrue() {
        try (MockedStatic<UserDAO> userDao = Mockito.mockStatic(UserDAO.class)) {
            userDao.when(() -> UserDAO.resetPassword(
                            "Alice",
                            "1990-01-01",
                            "alice",
                            "alice@example.com",
                            Mockito.anyString()))
                    .thenReturn(true);

            assertTrue(AuthService.resetPassword("Alice", "1990-01-01", "alice", "alice@example.com", "newpass"));
        }
    }

    @Test
    void resetPassword_failure_returnsFalse() {
        try (MockedStatic<UserDAO> userDao = Mockito.mockStatic(UserDAO.class)) {
            userDao.when(() -> UserDAO.resetPassword(
                            "Alice",
                            "1990-01-01",
                            "alice",
                            "alice@example.com",
                            Mockito.anyString()))
                    .thenReturn(false);

            assertFalse(AuthService.resetPassword("Alice", "1990-01-01", "alice", "alice@example.com", "newpass"));
        }
    }
}
