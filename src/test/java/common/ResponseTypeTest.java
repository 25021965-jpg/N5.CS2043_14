package common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ResponseTypeTest {
    @Test
    void testResponseTypeFrom() {
        // Test xem hàm from có tách được từ đầu tiên không
        assertEquals(ResponseType.LOGIN_SUCCESS, ResponseType.from("LOGIN_SUCCESS hoan_thanh"));
        assertEquals(ResponseType.ERROR, ResponseType.from("ERROR He_thong_ban"));
        assertNull(ResponseType.from("TIN_NHAN_LA")); // Trường hợp không tồn tại
    }
}