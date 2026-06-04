package common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommandTest {

    // ==================== NULL / BLANK ====================

    @Test
    void from_null_returnsNull() {
        assertNull(Command.from(null));
    }

    @Test
    void from_emptyString_returnsNull() {
        assertNull(Command.from(""));
    }

    @Test
    void from_blankSpaces_returnsNull() {
        assertNull(Command.from("   "));
    }

    // ==================== HAPPY PATH ====================

    @Test
    void from_validUpperCase_returnsCorrectCommand() {
        assertEquals(Command.LOGIN, Command.from("LOGIN"));
    }

    @Test
    void from_validLowerCase_returnsCorrectCommand() {
        assertEquals(Command.REGISTER, Command.from("register"));
    }

    @Test
    void from_validMixedCase_returnsCorrectCommand() {
        assertEquals(Command.BID, Command.from("Bid"));
    }

    @Test
    void from_validWithLeadingTrailingSpace_returnsCorrectCommand() {
        assertEquals(Command.LOGOUT, Command.from("  LOGOUT  "));
    }

    // ==================== ALL AUTH COMMANDS ====================

    @Test
    void from_allAuthCommands_parsed() {
        assertNotNull(Command.from("LOGIN"));
        assertNotNull(Command.from("REGISTER"));
        assertNotNull(Command.from("LOGOUT"));
        assertNotNull(Command.from("FORGOT_PASSWORD"));
    }

    // ==================== ALL AUCTION COMMANDS ====================

    @Test
    void from_allAuctionCommands_parsed() {
        assertNotNull(Command.from("LIST"));
        assertNotNull(Command.from("CREATE"));
        assertNotNull(Command.from("JOIN"));
        assertNotNull(Command.from("LEAVE"));
        assertNotNull(Command.from("BID"));
        assertNotNull(Command.from("GET_BID_HISTORY"));
    }

    // ==================== ADMIN COMMANDS ====================

    @Test
    void from_allAdminCommands_parsed() {
        assertNotNull(Command.from("LIST_USERS"));
        assertNotNull(Command.from("DELETE_USER"));
        assertNotNull(Command.from("UPDATE_USER_ROLE"));
        assertNotNull(Command.from("STOP_AUCTION"));
        assertNotNull(Command.from("RESUME_AUCTION"));
        assertNotNull(Command.from("CANCEL_AUCTION"));
        assertNotNull(Command.from("APPROVE_AUCTION"));
        assertNotNull(Command.from("LIST_PENDING_AUCTIONS"));
    }

    // ==================== UNKNOWN / ERROR PATH ====================

    @Test
    void from_unknownCommand_returnsNull() {
        assertNull(Command.from("UNKNOWN_XYZ"));
    }

    @Test
    void from_numericString_returnsNull() {
        assertNull(Command.from("123"));
    }

    @Test
    void from_specialChars_returnsNull() {
        assertNull(Command.from("!@#$"));
    }

    // ==================== BOUNDARY ====================

    @Test
    void from_singleChar_returnsNull() {
        assertNull(Command.from("X"));
    }

    @Test
    void from_commandWithPipeDelimiter_returnsNull() {
        assertNull(Command.from("LOGIN|user|pass"));
    }
}