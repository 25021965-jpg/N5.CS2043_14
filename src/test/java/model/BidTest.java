package model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * File: src/test/java/model/BidTest.java
 */
class BidTest {

    @Test
    void constructor_withUser_setsUsernameFromUser() {
        User user = new User();
        user.setUsername("alice");
        Bid bid = new Bid(user, new BigDecimal("500"));
        assertEquals("alice", bid.getUsername());
    }

    @Test
    void constructor_withNullUser_usernameEmpty() {
        Bid bid = new Bid(null, new BigDecimal("100"));
        assertEquals("", bid.getUsername());
    }

    @Test
    void constructor_setsDefaultStatusOutbid() {
        Bid bid = new Bid(null, new BigDecimal("100"));
        assertEquals("OUTBID", bid.getStatus());
    }

    @Test
    void constructor_setsAmountString() {
        Bid bid = new Bid(null, new BigDecimal("1500"));
        assertNotNull(bid.getAmountString());
        assertTrue(bid.getAmountString().contains("USD"));
    }

    @Test
    void constructor_setsTimeToNow() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Bid bid = new Bid(null, new BigDecimal("100"));
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertFalse(bid.getTime().isBefore(before));
        assertFalse(bid.getTime().isAfter(after));
    }

    // ==================== setAmount ====================

    @Test
    void setAmount_null_amountStringZeroUsd() {
        Bid bid = new Bid();
        bid.setAmount(null);
        assertEquals("0 USD", bid.getAmountString());
    }

    @Test
    void setAmount_zero_formattedCorrectly() {
        Bid bid = new Bid();
        bid.setAmount(BigDecimal.ZERO);
        assertEquals("0 USD", bid.getAmountString());
    }

    @Test
    void setAmount_largeAmount_containsUsd() {
        Bid bid = new Bid();
        bid.setAmount(new BigDecimal("1000000"));
        assertTrue(bid.getAmountString().endsWith("USD"));
        assertFalse(bid.getAmountString().isBlank());
    }

    @Test
    void setAmount_positiveAmount_formattedWithUsd() {
        Bid bid = new Bid();
        bid.setAmount(new BigDecimal("500"));
        assertTrue(bid.getAmountString().contains("500"));
        assertTrue(bid.getAmountString().endsWith("USD"));
    }

    // ==================== getTimeString ====================

    @Test
    void getTimeString_timeStringFieldSet_returnsFieldValue() {
        Bid bid = new Bid();
        bid.setTimeString("12:30:00");
        assertEquals("12:30:00", bid.getTimeString());
    }

    @Test
    void getTimeString_timeStringEmpty_formatsFromTime() {
        Bid bid = new Bid();
        bid.setTimeString("");
        bid.setTime(LocalDateTime.of(2024, 1, 1, 14, 30, 55));
        assertEquals("14:30:55", bid.getTimeString());
    }

    @Test
    void getTimeString_timeStringNull_formatsFromTime() {
        Bid bid = new Bid();
        bid.setTimeString(null);
        bid.setTime(LocalDateTime.of(2024, 6, 15, 9, 5, 3));
        assertEquals("09:05:03", bid.getTimeString());
    }

    @Test
    void getTimeString_bothNull_returnsEmpty() {
        Bid bid = new Bid();
        bid.setTimeString(null);
        bid.setTime(null);
        assertEquals("", bid.getTimeString());
    }

    @Test
    void defaultConstructor_allFieldsNull() {
        Bid bid = new Bid();
        assertNull(bid.getBidder());
        assertNull(bid.getAmount());
        assertNull(bid.getTime());
        assertNull(bid.getStatus());
    }
}