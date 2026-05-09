package utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationTest {

    // Giả lập hàm kiểm tra email
    public boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    @Test
    @DisplayName("Verify Email Validation Logic")
    void testEmailValidation() {
        assertTrue(isValidEmail("test@gmail.com"), "Valid email should return true");
        assertFalse(isValidEmail("testgmail.com"), "Email missing @ should return false");
        assertFalse(isValidEmail(null), "Null email should return false");
    }
}
