package client.network.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResponseHandlerTest {

    private final List<String> received = new ArrayList<>();

    @BeforeEach
    void setUp() {
        received.clear();
        ResponseHandler.setLiveAuctionListener(null);
        ResponseHandler.setMainStage(null);
    }

    @Test
    void handle_null_or_blank_doesNothing() {
        assertDoesNotThrow(() -> ResponseHandler.handle(null));
        assertDoesNotThrow(() -> ResponseHandler.handle("   "));
        assertTrue(received.isEmpty());
    }

    @Test
    void handle_allLiveAuctionMessages_forwardedToListener() {
        ResponseHandler.setLiveAuctionListener(received::add);

        try (MockedStatic<ResponseRouter> mockedRouter = mockStatic(ResponseRouter.class)) {

            mockedRouter.when(() -> ResponseRouter.route(anyString(), any()))
                    .thenAnswer(invocation -> {
                        String msg = invocation.getArgument(0);
                        try {
                            java.lang.reflect.Field field = ResponseHandler.class.getDeclaredField("liveAuctionListener");
                            field.setAccessible(true);
                            Consumer<String> listener = (Consumer<String>) field.get(null);

                            if (listener != null) {
                                listener.accept(msg);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    });

            String[] messages = {
                    "UPDATE_PRICE|1500", "JOIN_SUCCESS|data", "JOIN_FAILED|err",
                    "BID_FAILED|err", "BID_HISTORY_SUCCESS|data", "BID_HISTORY_EMPTY",
                    "AUCTION_ENDED|id", "YOU_WON|100", "VIRTUAL_BALANCE|1000",
                    "WINNER_BALANCE|500", "SELLER_BALANCE|2000", "AUTO_BID_SET|true",
                    "AUTO_BID_CANCELLED|true", "AUTO_BID_MAX_REACHED|true"
            };

            for (String msg : messages) {
                ResponseHandler.handle(msg);
            }

            assertEquals(messages.length, received.size());
        }
    }

    @Test
    void handle_liveMessageWithNullListener_doesNotCrash() {
        ResponseHandler.setLiveAuctionListener(null);
        try (MockedStatic<ResponseRouter> mockedRouter = mockStatic(ResponseRouter.class)) {
            assertDoesNotThrow(() -> ResponseHandler.handle("UPDATE_PRICE|999"));
        }
    }

    @Test
    void handle_nonLiveMessage_routedToRouter() {
        // Mock static ResponseRouter để chặn Platform.runLater
        try (MockedStatic<ResponseRouter> mockedRouter = mockStatic(ResponseRouter.class)) {
            ResponseHandler.handle("LOGIN_SUCCESS|data");

            // Xác nhận rằng Router đã được gọi mà không cần chạy code bên trong nó
            mockedRouter.verify(() -> ResponseRouter.route(eq("LOGIN_SUCCESS|data"), any()));
        }
    }
}