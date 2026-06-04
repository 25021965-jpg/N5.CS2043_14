package model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private Transaction tx(String type, BigDecimal amount, String relatedUsername) {
        Transaction t = new Transaction();
        t.setType(type);
        t.setAmount(amount);
        t.setRelatedUsername(relatedUsername);
        return t;
    }

    // ==================== getFormattedTime ====================

    @Test
    void getFormattedTime_nullCreatedAt_returnsEmpty() {
        Transaction t = new Transaction();
        assertEquals("", t.getFormattedTime());
    }

    @Test
    void getFormattedTime_validTimestamp_formattedDDMMYYYY() {
        Transaction t = new Transaction();
        t.setCreatedAt(Timestamp.valueOf(LocalDateTime.of(2024, 12, 31, 23, 59)));
        assertEquals("31/12/2024 - 23:59", t.getFormattedTime());
    }

    // ==================== getDisplayTitle ====================

    @Test
    void getDisplayTitle_deposit_returnsDepositSuccess() {
        Transaction t = tx("DEPOSIT", BigDecimal.TEN, null);
        assertEquals("Deposit Successfully", t.getDisplayTitle());
    }

    @Test
    void getDisplayTitle_withdraw_returnsWithdrawSuccess() {
        Transaction t = tx("WITHDRAW", BigDecimal.TEN, null);
        assertEquals("Withdraw Successfully", t.getDisplayTitle());
    }

    @Test
    void getDisplayTitle_transferOut_includesRelatedUsername() {
        Transaction t = tx("TRANSFER_OUT", BigDecimal.TEN, "bob");
        assertEquals("Paid to bob", t.getDisplayTitle());
    }

    @Test
    void getDisplayTitle_transferIn_includesRelatedUsername() {
        Transaction t = tx("TRANSFER_IN", BigDecimal.TEN, "alice");
        assertEquals("Received from alice", t.getDisplayTitle());
    }

    @Test
    void getDisplayTitle_unknownType_returnsTypeItself() {
        Transaction t = tx("BONUS", BigDecimal.TEN, null);
        assertEquals("BONUS", t.getDisplayTitle());
    }

    @Test
    void getDisplayTitle_nullRelatedUsername_transferOutShowsNull() {
        Transaction t = tx("TRANSFER_OUT", BigDecimal.TEN, null);
        assertEquals("Paid to null", t.getDisplayTitle());
    }

    // ==================== isPositive ====================

    @Test
    void isPositive_deposit_returnsTrue() {
        assertTrue(tx("DEPOSIT", BigDecimal.TEN, null).isPositive());
    }

    @Test
    void isPositive_transferIn_returnsTrue() {
        assertTrue(tx("TRANSFER_IN", BigDecimal.TEN, null).isPositive());
    }

    @Test
    void isPositive_withdraw_returnsFalse() {
        assertFalse(tx("WITHDRAW", BigDecimal.TEN, null).isPositive());
    }

    @Test
    void isPositive_transferOut_returnsFalse() {
        assertFalse(tx("TRANSFER_OUT", BigDecimal.TEN, null).isPositive());
    }

    @Test
    void isPositive_unknown_returnsFalse() {
        assertFalse(tx("BONUS", BigDecimal.TEN, null).isPositive());
    }

    // ==================== getFormattedAmount ====================

    @Test
    void getFormattedAmount_deposit_startsWithPlus() {
        Transaction t = tx("DEPOSIT", new BigDecimal("500"), null);
        assertTrue(t.getFormattedAmount().startsWith("+ "));
        assertTrue(t.getFormattedAmount().endsWith("USD"));
    }

    @Test
    void getFormattedAmount_withdraw_startsWithMinus() {
        Transaction t = tx("WITHDRAW", new BigDecimal("200"), null);
        assertTrue(t.getFormattedAmount().startsWith("- "));
        assertTrue(t.getFormattedAmount().endsWith("USD"));
    }

    @Test
    void getFormattedAmount_largeDeposit_includesThousandSeparator() {
        Transaction t = tx("DEPOSIT", new BigDecimal("1000000"), null);
        String formatted = t.getFormattedAmount();
        boolean hasSeparator = formatted.contains(",") || formatted.contains(".");
        assertTrue(hasSeparator, "Formatted amount should contain a thousand separator");
    }

    @Test
    void getFormattedAmount_zeroWithdraw_formatsZero() {
        Transaction t = tx("WITHDRAW", BigDecimal.ZERO, null);
        assertTrue(t.getFormattedAmount().contains("0"));
    }
}