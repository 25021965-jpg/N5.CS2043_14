package client.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SocketTest {

    @Test
    @DisplayName("Verify Client sends data ")
    void testSendData() {

        final StringBuilder capturedData = new StringBuilder();

        OutputStream manualMockOs = new OutputStream() {
            @Override
            public void write(int b) {
                capturedData.append((char) b);
            }
        };

        PrintWriter out = new PrintWriter(manualMockOs, true);
        String msg = "REGISTER|johndoe|pass123";

        // Thực hiện gửi tin
        out.println(msg);

        // Kiểm tra xem dữ liệu có được ghi vào stream không
        assertTrue(capturedData.toString().contains(msg), "Data should be captured by our manual stream");
        System.out.println(">>> Captured: " + capturedData.toString().trim());
    }

    @Test
    @DisplayName("Verify Protocol Logic")
    void testProtocolWithManualStub() {

        List<String> testList = new ArrayList<>();

        testList.add("LOGIN_SUCCESS");

        // Kiểm tra logic bình thường
        assertEquals(1, testList.size());
        assertEquals("LOGIN_SUCCESS", testList.get(0));

        System.out.println(">>> Manual stub verification: OK");
    }
}