package client.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ResponseHandlerTest {

    // Hàm giả lập logic xử lý tin nhắn từ Server về Client
    private String handleServerResponse(String message) {
        if (message == null || message.isEmpty()) return "EMPTY_MSG";

        String[] parts = message.split("\\|");
        String status = parts[0];

        switch (status) {
            case "REGISTER_SUCCESS":
                return "MOVE_TO_LOGIN_SCREEN";
            case "REGISTER_FAILED":
                return "DISPLAY_ERROR_POPUP";
            case "LOGIN_SUCCESS":
                return "ENTER_CHAT_ROOM";
            case "LOGIN_FAILED":
                return "AUTH_FAILED";
            default:
                return "UNKNOWN_PROTOCOL";
        }
    }

    @Test
    @DisplayName("Verify UI action when registration is successful")
    void testHandleRegisterSuccess() {
        // Chạy thử với tin nhắn thành công
        String action = handleServerResponse("REGISTER_SUCCESS");
        assertEquals("MOVE_TO_LOGIN_SCREEN", action, "Should navigate to login on success");
    }

    @Test
    @DisplayName("Verify UI action when registration fails")
    void testHandleRegisterFail() {
        // Chạy thử với tin nhắn thất bại kèm lý do
        String action = handleServerResponse("REGISTER_FAILED|Username_exists");
        assertEquals("DISPLAY_ERROR_POPUP", action, "Should show error message to user");
    }

    @Test
    @DisplayName("Verify handling of unknown server messages")
    void testHandleUnknownMessage() {
        // Test trường hợp Server gửi tin nhắn rác hoặc lỗi format
        String action = handleServerResponse("SOME_WEIRD_STRING");
        assertEquals("UNKNOWN_PROTOCOL", action, "Should handle malformed strings gracefully");
    }
}