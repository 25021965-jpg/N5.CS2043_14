package client.network;

import common.Command;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageParserTest {

    // ==================== HAPPY PATH ====================

    @Test
    void parse_loginCommand_commandIsLogin() {
        MessageParser mp = new MessageParser("LOGIN|alice|password");
        assertEquals(Command.LOGIN, mp.getCommand());
    }

    @Test
    void parse_loginCommand_argsCorrect() {
        MessageParser mp = new MessageParser("LOGIN|alice|password");
        assertArrayEquals(new String[]{"alice", "password"}, mp.getArgs());
    }

    @Test
    void parse_noArgs_emptyArgsArray() {
        MessageParser mp = new MessageParser("LIST");
        assertEquals(Command.LIST, mp.getCommand());
        assertEquals(0, mp.getArgs().length);
    }

    @Test
    void parse_singleArg_argParsedCorrectly() {
        MessageParser mp = new MessageParser("BID|500");
        assertEquals(Command.BID, mp.getCommand());
        assertArrayEquals(new String[]{"500"}, mp.getArgs());
    }

    @Test
    void parse_manyArgs_allArgsPresent() {
        MessageParser mp = new MessageParser("REGISTER|Alice|alice|alice@mail.com|pass123|1990-01-01");
        assertEquals(Command.REGISTER, mp.getCommand());
        assertEquals(5, mp.getArgs().length);
        assertEquals("Alice", mp.getArgs()[0]);
        assertEquals("1990-01-01", mp.getArgs()[4]);
    }

    // ==================== UNKNOWN COMMAND ====================

    @Test
    void parse_unknownCommand_commandIsNull() {
        MessageParser mp = new MessageParser("UNKNOWN_CMD|arg");
        assertNull(mp.getCommand());
        // args still parsed
        assertArrayEquals(new String[]{"arg"}, mp.getArgs());
    }

    // ==================== EDGE CASES ====================

    @Test
    void parse_emptyArg_emptyStringInArgs() {
        MessageParser mp = new MessageParser("LOGIN||password");
        assertEquals("", mp.getArgs()[0]);
        assertEquals("password", mp.getArgs()[1]);
    }

    @Test
    void parse_lowercaseCommand_returnsCorrectCommand() {
        MessageParser mp = new MessageParser("login|user|pass");
        assertEquals(Command.LOGIN, mp.getCommand());
    }

    @Test
    void parse_commandOnly_argsLengthZero() {
        MessageParser mp = new MessageParser("LOGOUT");
        assertEquals(0, mp.getArgs().length);
    }

    @Test
    void parse_emptyString_commandNull() {
        MessageParser mp = new MessageParser("");
        assertNull(mp.getCommand());
        assertEquals(0, mp.getArgs().length);
    }
}