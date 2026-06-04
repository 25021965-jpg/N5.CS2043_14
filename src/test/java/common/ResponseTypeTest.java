package common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResponseTypeTest {

    // ==================== NULL / BLANK ====================

    @Test
    void from_null_returnsNull() {
        assertNull(ResponseType.from(null));
    }

    @Test
    void from_emptyString_returnsNull() {
        assertNull(ResponseType.from(""));
    }

    @Test
    void from_blankString_returnsNull() {
        assertNull(ResponseType.from("   "));
    }

    // ==================== HAPPY PATH — bare type ====================

    @Test
    void from_loginSuccess_returnsCorrect() {
        assertEquals(ResponseType.LOGIN_SUCCESS, ResponseType.from("LOGIN_SUCCESS"));
    }

    @Test
    void from_loginFailed_returnsCorrect() {
        assertEquals(ResponseType.LOGIN_FAILED, ResponseType.from("LOGIN_FAILED"));
    }

    @Test
    void from_lowerCase_parsed() {
        assertEquals(ResponseType.BID_SUCCESS, ResponseType.from("bid_success"));
    }

    @Test
    void from_mixedCase_parsed() {
        assertEquals(ResponseType.ERROR, ResponseType.from("Error"));
    }

    @Test
    void from_withLeadingTrailingWhitespace_parsed() {
        assertEquals(ResponseType.DISCONNECTED, ResponseType.from("  DISCONNECTED  "));
    }

    // ==================== WITH PIPE PAYLOAD (typical server message) ====================

    @Test
    void from_messageWithPayload_extractsTypeOnly() {
        assertEquals(ResponseType.LOGIN_SUCCESS, ResponseType.from("LOGIN_SUCCESS|userId|username"));
    }

    @Test
    void from_updatePriceWithAmount_extractsType() {
        assertEquals(ResponseType.UPDATE_PRICE, ResponseType.from("UPDATE_PRICE|1500.00"));
    }

    @Test
    void from_bidHistorySuccess_withData() {
        assertEquals(ResponseType.BID_HISTORY_SUCCESS, ResponseType.from("BID_HISTORY_SUCCESS|alice;500;12:00:00"));
    }

    // ==================== ALL AUTH RESPONSES ====================

    @Test
    void from_allAuthTypes_parsed() {
        assertNotNull(ResponseType.from("REGISTER_SUCCESS"));
        assertNotNull(ResponseType.from("REGISTER_FAILED"));
        assertNotNull(ResponseType.from("FORGOT_SUCCESS"));
        assertNotNull(ResponseType.from("FORGOT_FAILED"));
    }

    // ==================== AUCTION ADMIN RESPONSES ====================

    @Test
    void from_auctionAdminTypes_parsed() {
        assertNotNull(ResponseType.from("STOP_AUCTION_SUCCESS"));
        assertNotNull(ResponseType.from("RESUME_AUCTION_SUCCESS"));
        assertNotNull(ResponseType.from("CANCEL_AUCTION_SUCCESS"));
        assertNotNull(ResponseType.from("APPROVE_AUCTION_SUCCESS"));
        assertNotNull(ResponseType.from("APPROVE_AUCTION_FAILED"));
    }

    // ==================== ERROR PATH ====================

    @Test
    void from_unknownType_returnsNull() {
        assertNull(ResponseType.from("NOT_A_REAL_TYPE"));
    }

    @Test
    void from_numericString_returnsNull() {
        assertNull(ResponseType.from("12345"));
    }
}