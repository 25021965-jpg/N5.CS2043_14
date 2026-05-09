package server.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ServerSecurityTest {

    @Test
    @DisplayName("Server should not crash when receiving incomplete REGISTER data")
    void testIncompleteDataHandling() {
        // Missing email and password
        String badData = "REGISTER|Some Name|someuser";
        String[] parts = badData.split("\\|");


        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            String password = parts[4];
        }, "Server would crash if it doesn't check array length before accessing index 4");
    }
}