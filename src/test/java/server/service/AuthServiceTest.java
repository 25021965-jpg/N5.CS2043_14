package server.service;

import model.Role;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    @BeforeEach
    void setUp() {
        AuthService.logout("test-session-1");
        AuthService.logout("test-session-2");
        AuthService.logout("no-such-session");
    }

    private User makeUser(String id, String username) {
        User u = new User();
        u.setUser_id(id);
        u.setUsername(username);
        u.setRole(Role.BIDDER);
        return u;
    }

    // ==================== addSession + getUser ====================

    @Test
    void addSession_thenGetUser_returnsCorrectUser() {
        User user = makeUser("u1", "alice");
        AuthService.addSession("test-session-1", user);
        User found = AuthService.getUser("test-session-1");
        assertNotNull(found);
        assertEquals("alice", found.getUsername());
    }

    @Test
    void getUser_unknownSession_returnsNull() {
        assertNull(AuthService.getUser("nonexistent-session-xyz"));
    }

    @Test
    void getUser_afterLogout_returnsNull() {
        AuthService.addSession("test-session-1", makeUser("u1", "alice"));
        AuthService.logout("test-session-1");
        assertNull(AuthService.getUser("test-session-1"));
    }

    // ==================== logout ====================

    @Test
    void logout_nonExistentSession_noException() {
        assertDoesNotThrow(() -> AuthService.logout("no-such-session"));
    }

    @Test
    void logout_existingSession_removed() {
        AuthService.addSession("test-session-1", makeUser("u1", "bob"));
        AuthService.logout("test-session-1");
        assertNull(AuthService.getUser("test-session-1"));
    }

    // ==================== Multiple sessions ====================

    @Test
    void multipleSessions_independentOfEachOther() {
        AuthService.addSession("test-session-1", makeUser("u1", "alice"));
        AuthService.addSession("test-session-2", makeUser("u2", "bob"));

        assertEquals("alice", AuthService.getUser("test-session-1").getUsername());
        assertEquals("bob", AuthService.getUser("test-session-2").getUsername());

        AuthService.logout("test-session-1");

        assertNull(AuthService.getUser("test-session-1"));
        assertNotNull(AuthService.getUser("test-session-2"));

        AuthService.logout("test-session-2");
    }

    @Test
    void addSession_overwrite_updatesSession() {
        User user1 = makeUser("u1", "alice");
        User user2 = makeUser("u2", "bob");

        AuthService.addSession("test-session-1", user1);
        AuthService.addSession("test-session-1", user2); // overwrite

        assertEquals("bob", AuthService.getUser("test-session-1").getUsername());
        AuthService.logout("test-session-1");
    }
}