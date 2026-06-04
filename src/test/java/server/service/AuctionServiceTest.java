package server.service;

import model.Entity.Item.Electronics;
import model.Entity.Item.Item;
import model.Entity.User.Role;
import model.Entity.User.Seller;
import model.Entity.User.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AuctionServiceTest {

    private User makeSeller() {
        User u = new Seller();
        u.setUser_id("seller-1");
        u.setUsername("seller");
        u.setRole(Role.SELLER);
        return u;
    }

    private Item makeItem() {
        Item item = new Electronics();
        item.setItem_id("item-1");
        item.setName("Watch");
        return item;
    }

    // ==================== createAuction — date parsing ====================

    @Test
    void createAuction_invalidStartDate_throwsRuntimeException() {
        assertThrows(RuntimeException.class, () ->
                AuctionService.createAuction(
                        makeSeller(),
                        makeItem(),
                        new BigDecimal("100"),
                        new BigDecimal("10"),
                        "BAD_DATE",
                        "2024-12-31T23:59:59"
                )
        );
    }

    @Test
    void createAuction_invalidEndDate_throwsRuntimeException() {
        assertThrows(RuntimeException.class, () ->
                AuctionService.createAuction(
                        makeSeller(),
                        makeItem(),
                        new BigDecimal("100"),
                        new BigDecimal("10"),
                        "2024-01-01T10:00:00",
                        "NOT_A_DATE"
                )
        );
    }

    @Test
    void createAuction_nullDates_throwsRuntimeException() {
        assertThrows(RuntimeException.class, () ->
                AuctionService.createAuction(
                        makeSeller(),
                        makeItem(),
                        new BigDecimal("100"),
                        new BigDecimal("10"),
                        null,
                        null
                )
        );
    }

    @Test
    void createAuction_wrongDateFormat_throwsRuntimeException() {
        assertThrows(RuntimeException.class, () ->
                AuctionService.createAuction(
                        makeSeller(),
                        makeItem(),
                        new BigDecimal("100"),
                        new BigDecimal("10"),
                        "31/12/2024 23:59",
                        "01/01/2025 00:00"
                )
        );
    }
}