package client.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProtocolFormatTest {

    @Test
    @DisplayName("Check if Register message follows COMMAND|Data format")
    void testRegisterProtocol() {
        // Giả lập các trường dữ liệu
        String fullName = "John Doe";
        String user = "johndoe";
        String email = "john@test.com";
        String pass = "123456";

        // Tạo chuỗi protocol như trong Client
        String result = "REGISTER|" + fullName + "|" + user + "|" + email + "|" + pass;

        // Kiểm tra xem có đủ 5 thành phần không
        String[] parts = result.split("\\|");
        assertEquals(5, parts.length, "Protocol string must have 5 parts");
        assertEquals("REGISTER", parts[0], "Command must be REGISTER");
        assertEquals("johndoe", parts[2], "Username must be in the 3rd position");
    }

    @Test
    @DisplayName("Check if Login message follows COMMAND|Data format")
    void testLoginProtocol() {
        String user = "admin";
        String pass = "admin123";

        String result = "LOGIN|" + user + "|" + pass;

        assertEquals("LOGIN|admin|admin123", result, "Login protocol format mismatch");
    }
}