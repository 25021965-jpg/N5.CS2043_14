package model;

import model.Entity.User.Bidder;
import model.Entity.User.Role;
import model.Entity.User.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    // ==================== Default role ====================

    @Test
    void defaultConstructor_roleIsBidder() {
        User u = new Bidder();
        assertEquals(Role.BIDDER, u.getRole());
    }

    // ==================== hasRole() ====================

    @Test
    void hasRole_bidderRequiresBidder_returnsTrue() {
        User u = new Bidder();
        u.setRole(Role.BIDDER);
        assertTrue(u.hasRole(Role.BIDDER));
    }

    @Test
    void hasRole_bidderRequiresSeller_returnsFalse() {
        User u = new Bidder();
        u.setRole(Role.BIDDER);
        assertFalse(u.hasRole(Role.SELLER));
    }

    @Test
    void hasRole_adminRequiresBidder_returnsTrue() {
        User u = new Bidder();
        u.setRole(Role.ADMIN);
        assertTrue(u.hasRole(Role.BIDDER));
    }

    @Test
    void hasRole_adminRequiresSeller_returnsTrue() {
        User u = new Bidder();
        u.setRole(Role.ADMIN);
        assertTrue(u.hasRole(Role.SELLER));
    }

    @Test
    void hasRole_adminRequiresAdmin_returnsTrue() {
        User u = new Bidder();
        u.setRole(Role.ADMIN);
        assertTrue(u.hasRole(Role.ADMIN));
    }

    @Test
    void hasRole_sellerRequiresBidder_returnsFalse() {
        User u = new Bidder();
        u.setRole(Role.SELLER);
        assertFalse(u.hasRole(Role.BIDDER));
    }

    // ==================== addTransaction / getTransactions ====================

    @Test
    void addTransaction_addsToList() {
        User u = new Bidder();
        u.addTransaction("tx-1");

        assertEquals(1, u.getTransactions().size());
        assertEquals("tx-1", u.getTransactions().get(0));
    }

    @Test
    void addTransaction_multiple_allStored() {
        User u = new Bidder();

        u.addTransaction("tx-1");
        u.addTransaction("tx-2");
        u.addTransaction("tx-3");

        assertEquals(3, u.getTransactions().size());
    }

    // ==================== setters / getters ====================

    @Test
    void setBalance_null_balanceIsNull() {
        User u = new Bidder();

        u.setBalance(null);

        assertNull(u.getBalance());
    }

    @Test
    void setBalance_value_storedCorrectly() {
        User u = new Bidder();

        u.setBalance(new BigDecimal("9999.99"));

        assertEquals(new BigDecimal("9999.99"), u.getBalance());
    }

    @Test
    void setVerified_trueAndFalse() {
        User u = new Bidder();

        u.setVerified(true);
        assertTrue(u.isVerified());

        u.setVerified(false);
        assertFalse(u.isVerified());
    }
}