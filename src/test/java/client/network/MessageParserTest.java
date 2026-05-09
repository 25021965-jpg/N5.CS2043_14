package client.network;

import common.Command;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MessageParserTest {

    @Test
    @DisplayName("Test 1: Parse Login command")
    void testParseLogin() {
        // Thay LOGIN_SUCCESS bằng LOGIN (vì Enum Command có LOGIN)
        String rawMsg = "LOGIN|user123|pass123";
        MessageParser parser = new MessageParser(rawMsg);

        // Bây giờ Command.from("LOGIN") sẽ trả về Command.LOGIN (hết null)
        assertNotNull(parser.getCommand());
        assertEquals(Command.LOGIN, parser.getCommand());

        String[] args = parser.getArgs();
        assertEquals(2, args.length);
        assertEquals("user123", args[0]);
        assertEquals("pass123", args[1]);

        System.out.println(">>> Parse Login: OK");
    }

    @Test
    @DisplayName("Test 2: Parse Bid command")
    void testParseBid() {
        // Thử với lệnh BID có trong Enum của bạn
        String rawMsg = "BID|item01|500";
        MessageParser parser = new MessageParser(rawMsg);

        assertEquals(Command.BID, parser.getCommand());
        assertEquals("item01", parser.getArgs()[0]);
        assertEquals("500", parser.getArgs()[1]);

        System.out.println(">>> Parse Bid: OK");
    }
}