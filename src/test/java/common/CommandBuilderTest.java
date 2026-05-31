package common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommandBuilderTest {

    @Test
    void build_noArgs_returnsCommandName() {
        String result = CommandBuilder.build(Command.LIST);
        assertEquals("LIST", result);
    }

    @Test
    void build_singleArg_appendsWithPipe() {
        String result = CommandBuilder.build(Command.LOGIN, "alice");
        assertEquals("LOGIN|alice", result);
    }

    @Test
    void build_multipleArgs_appendsAllWithPipe() {
        String result = CommandBuilder.build(Command.LOGIN, "alice", "password123");
        assertEquals("LOGIN|alice|password123", result);
    }

    @Test
    void build_threeArgs_allPresent() {
        String result = CommandBuilder.build(Command.BID, "auction-1", "500", "USD");
        assertEquals("BID|auction-1|500|USD", result);
    }

    // ==================== NULL / EMPTY ARG ====================

    @Test
    void build_nullArg_treatedAsEmpty() {
        String result = CommandBuilder.build(Command.LOGIN, null, "pass");
        assertEquals("LOGIN||pass", result);
    }

    @Test
    void build_emptyArg_preservedAsEmpty() {
        String result = CommandBuilder.build(Command.REGISTER, "", "user");
        assertEquals("REGISTER||user", result);
    }

    @Test
    void build_allNullArgs_allEmptySegments() {
        String result = CommandBuilder.build(Command.DEPOSIT, null, null);
        assertEquals("DEPOSIT||", result);
    }

    // ==================== BOUNDARY ====================

    @Test
    void build_argWithPipeChar_notEscaped() {
        String result = CommandBuilder.build(Command.CREATE, "name|with|pipes");
        assertEquals("CREATE|name|with|pipes", result);
    }

    @Test
    void build_longArg_noTruncation() {
        String longArg = "a".repeat(1000);
        String result = CommandBuilder.build(Command.CREATE, longArg);
        assertTrue(result.startsWith("CREATE|"));
        assertTrue(result.length() > 1000);
    }
}