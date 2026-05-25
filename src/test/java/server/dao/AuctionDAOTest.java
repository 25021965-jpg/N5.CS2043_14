package server.dao;

import model.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuctionDAOTest {

    private static String testId;

    @BeforeAll
    static void setup() {

        testId =
                "T_AUC_" +
                        UUID.randomUUID()
                                .toString()
                                .substring(0, 8);

        try (
                Connection conn =
                        DatabaseService.getConnection();

                Statement stmt =
                        conn.createStatement()
        ) {

            stmt.executeUpdate("""
                    INSERT IGNORE INTO users
                    (user_id, username, password, fullname)
                    VALUES
                    ('user01', 'test_seller', '123', 'Seller Test')
                    """);

            stmt.executeUpdate("""
                    INSERT IGNORE INTO items
                    (item_id, name, description, category)
                    VALUES
                    ('item01', 'San pham Test', 'Mo ta test', 'OTHER')
                    """);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Save auction")
    void testSaveAuction() {

        Item item = new Item();
        item.setItem_id("item01");

        User seller = new User();
        seller.setUser_id("user01");

        Auction auction = new Auction();

        auction.setAuction_id(testId);
        auction.setItem(item);
        auction.setSeller(seller);

        auction.setStartingPrice(
                new BigDecimal("1000")
        );

        auction.setCurrentPrice(
                new BigDecimal("1500")
        );

        auction.setMinIncrement(
                new BigDecimal("50")
        );

        auction.setStartTime(
                LocalDateTime.now()
        );

        auction.setEndTime(
                LocalDateTime.now().plusDays(1)
        );

        assertDoesNotThrow(
                () -> AuctionDAO.save(auction)
        );
    }

    @Test
    @Order(2)
    @DisplayName("Approve auction")
    void testApproveAuction() {

        boolean result =
                AuctionDAO.approveAuction(testId);

        assertTrue(result);
    }

    @Test
    @Order(3)
    @DisplayName("Find all auctions")
    void testFindAll() {

        List<Auction> list =
                AuctionDAO.findAll();

        assertNotNull(list);

        boolean found =
                list.stream()
                        .anyMatch(a ->
                                a.getAuction_id()
                                        .equals(testId)
                        );

        assertTrue(found);
    }

    @Test
    @Order(4)
    @DisplayName("Stop auction")
    void testStopAuction() {

        boolean result =
                AuctionDAO.stopAuction(testId);

        assertTrue(result);
    }

    @AfterAll
    static void tearDown() {

        try (
                Connection conn =
                        DatabaseService.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement("""
                                DELETE FROM auctions
                                WHERE auction_id = ?
                                """)
        ) {

            ps.setString(1, testId);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}