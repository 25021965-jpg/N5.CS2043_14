package client.network.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResponseHandlerTest {

    private final List<String> received = new ArrayList<>();

    @BeforeEach
    void setUp() {
        received.clear();
        ResponseHandler.setLiveAuctionListener(null);
        ResponseHandler.setMainStage(null);
    }

    // ==================== NULL / BLANK GUARD ====================

    @Test
    void handle_null_doesNothing() {
        ResponseHandler.setLiveAuctionListener(received::add);
        assertDoesNotThrow(() -> ResponseHandler.handle(null));
        assertTrue(received.isEmpty());
    }

    @Test
    void handle_blank_doesNothing() {
        ResponseHandler.setLiveAuctionListener(received::add);
        assertDoesNotThrow(() -> ResponseHandler.handle("   "));
        assertTrue(received.isEmpty());
    }

    // ==================== LIVE AUCTION FAST-PATH ====================

    @Test
    void handle_updatePrice_forwardedToListener() {
        ResponseHandler.setLiveAuctionListener(received::add);
        ResponseHandler.handle("UPDATE_PRICE|1500");
        assertEquals(1, received.size());
        assertEquals("UPDATE_PRICE|1500", received.get(0));
    }

    @Test
    void handle_joinSuccess_forwardedToListener() {
        ResponseHandler.setLiveAuctionListener(received::add);
        ResponseHandler.handle("JOIN_SUCCESS|data");
        assertEquals(1, received.size());
    }

    @Test
    void handle_joinFailed_forwardedToListener() {
        ResponseHandler.setLiveAuctionListener(received::add);
        ResponseHandler.handle("JOIN_FAILED|reason");
        assertEquals(1, received.size());
    }

    @Test
    void handle_bidFailed_forwardedToListener() {
        ResponseHandler.setLiveAuctionListener(received::add);
        ResponseHandler.handle("BID_FAILED|reason");
        assertEquals(1, received.size());
    }

    @Test
    void handle_bidHistorySuccess_forwardedToListener() {
        ResponseHandler.setLiveAuctionListener(received::add);
        ResponseHandler.handle("BID_HISTORY_SUCCESS|alice;500;12:00:00");
        assertEquals(1, received.size());
    }

    @Test
    void handle_bidHistoryEmpty_forwardedToListener() {
        ResponseHandler.setLiveAuctionListener(received::add);
        ResponseHandler.handle("BID_HISTORY_EMPTY");
        assertEquals(1, received.size());
    }

    @Test
    void handle_auctionEnded_forwardedToListener() {
        ResponseHandler.setLiveAuctionListener(received::add);
        ResponseHandler.handle("AUCTION_ENDED|auction-1");
        assertEquals(1, received.size());
    }

    @Test
    void handle_youWon_forwardedToListener() {
        ResponseHandler.setLiveAuctionListener(received::add);
        ResponseHandler.handle("YOU_WON|1500");
        assertEquals(1, received.size());
    }

    @Test
    void handle_error_forwardedToListener() {
        ResponseHandler.setLiveAuctionListener(received::add);
        ResponseHandler.handle("ERROR|something went wrong");
        assertEquals(1, received.size());
    }

    // ==================== LISTENER NULL GUARD ====================

    @Test
    void handle_liveMessageWithNullListener_doesNotCrash() {
        ResponseHandler.setLiveAuctionListener(null);
        // Should not throw even though listener is null
        assertDoesNotThrow(() -> ResponseHandler.handle("UPDATE_PRICE|999"));
    }
}