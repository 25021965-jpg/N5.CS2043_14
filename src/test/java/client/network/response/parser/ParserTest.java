package client.network.response.parser;

import model.*;
import model.Entity.Item.Category;
import model.Entity.User.Role;
import model.Entity.User.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    private String makeAuctionToken(String id, String status) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String start = LocalDateTime.now().minusHours(2).format(fmt);
        String end   = LocalDateTime.now().plusHours(2).format(fmt);
        return id + ";i1;Watch;500;10;NO_IMAGE;" + start + ";" + end + ";ELECTRONICS;Nice watch;" + status + ";seller-1";
    }

    // ==================================================
    //  AuctionParser
    // ==================================================

    @Test
    void auctionParser_null_returnsEmptyList() {
        assertTrue(AuctionParser.parseList(null).isEmpty());
    }

    @Test
    void auctionParser_blank_returnsEmptyList() {
        assertTrue(AuctionParser.parseList("   ").isEmpty());
    }

    @Test
    void auctionParser_tooFewFields_skipsToken() {
        assertTrue(AuctionParser.parseList("id;itemId;itemName").isEmpty());
    }

    @Test
    void auctionParser_validToken_parsesOneAuction() {
        String token = makeAuctionToken("a1", "ACTIVE");
        List<Auction> result = AuctionParser.parseList(token);
        assertEquals(1, result.size());
        Auction a = result.get(0);
        assertEquals("a1", a.getAuction_id());
        assertEquals("Watch", a.getItem().getName());
        assertEquals(0, new BigDecimal("500").compareTo(a.getCurrentPrice()));
        assertEquals(Category.ELECTRONICS, a.getItem().getCategory());
        assertEquals(AuctionStatus.ACTIVE, a.getStatus());
        assertEquals("seller-1", a.getSeller().getUser_id());
    }

    @Test
    void auctionParser_invalidStatus_fallsToPending() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String start = LocalDateTime.now().minusHours(1).format(fmt);
        String end   = LocalDateTime.now().plusHours(1).format(fmt);
        String token = "a2;i2;Ring;200;5;NO_IMAGE;" + start + ";" + end + ";JEWELRY;Gold ring;GARBAGE_STATUS;seller-2";
        List<Auction> result = AuctionParser.parseList(token);
        assertEquals(1, result.size());
        assertEquals(AuctionStatus.PENDING_APPROVAL, result.get(0).getStatus());
    }

    @Test
    void auctionParser_invalidCategory_fallsToOther() {
        String token = makeAuctionToken("a3", "ACTIVE")
                .replace("ELECTRONICS", "NOT_A_CATEGORY");
        List<Auction> result = AuctionParser.parseList(token);
        assertEquals(1, result.size());
        assertEquals(Category.OTHER, result.get(0).getItem().getCategory());
    }

    @Test
    void auctionParser_withImages_parsedCorrectly() {
        String token = makeAuctionToken("a4", "ACTIVE")
                .replace("NO_IMAGE", "http://img1.jpg,http://img2.jpg");
        List<Auction> result = AuctionParser.parseList(token);
        List<String> images = result.get(0).getItem().getImages();
        assertEquals(2, images.size());
        assertEquals("http://img1.jpg", images.get(0));
    }

    @Test
    void auctionParser_noImageKeyword_emptyImages() {
        String token = makeAuctionToken("a5", "ACTIVE");
        assertTrue(AuctionParser.parseList(token).get(0).getItem().getImages().isEmpty());
    }

    @Test
    void auctionParser_multipleTokens_parsedAll() {
        String t1 = makeAuctionToken("a1", "ACTIVE");
        String t2 = makeAuctionToken("a2", "ENDED");
        List<Auction> result = AuctionParser.parseList(t1 + "|" + t2);
        assertEquals(2, result.size());
    }

    @Test
    void auctionParser_badPriceInToken_skipsToken() {
        String token = makeAuctionToken("a6", "ACTIVE")
                .replaceFirst(";500;", ";BAD_PRICE;");
        assertTrue(AuctionParser.parseList(token).isEmpty());
    }

    @Test
    void auctionParser_badDateInToken_skipsToken() {
        String token = "a7;i7;Item;100;10;NO_IMAGE;BAD_DATE;2099-12-31T23:59:59;ELECTRONICS;d;ACTIVE;s7";
        assertTrue(AuctionParser.parseList(token).isEmpty());
    }

    // ==================================================
    //  BidParser
    // ==================================================

    @Test
    void bidParser_null_returnsEmpty() {
        assertTrue(BidParser.parse(null).isEmpty());
    }

    @Test
    void bidParser_blank_returnsEmpty() {
        assertTrue(BidParser.parse("   ").isEmpty());
    }

    @Test
    void bidParser_singleBid_parsedCorrectly() {
        List<Bid> bids = BidParser.parse("alice;500;12:30:00");
        assertEquals(1, bids.size());
        assertEquals("alice", bids.get(0).getUsername());
        assertEquals(0, new BigDecimal("500").compareTo(bids.get(0).getAmount()));
        assertEquals("12:30:00", bids.get(0).getTimeString());
    }

    @Test
    void bidParser_multipleBids_allParsed() {
        List<Bid> bids = BidParser.parse("alice;500;12:00:00|bob;600;12:05:00|charlie;750;12:10:00");
        assertEquals(3, bids.size());
    }

    @Test
    void bidParser_tooFewFields_skipped() {
        assertTrue(BidParser.parse("alice;500").isEmpty());
    }

    @Test
    void bidParser_badAmount_skipped() {
        assertTrue(BidParser.parse("alice;NOT_A_NUMBER;12:00:00").isEmpty());
    }

    @Test
    void bidParser_mixedGoodAndBad_onlyGoodParsed() {
        List<Bid> bids = BidParser.parse("alice;500;12:00:00|bad|bob;600;12:05:00");
        assertEquals(2, bids.size());
    }

    // ==================================================
    //  ItemParser
    // ==================================================

    @Test
    void itemParser_null_returnsEmpty() {
        assertTrue(ItemParser.parse(null, 3).isEmpty());
    }

    @Test
    void itemParser_blank_returnsEmpty() {
        assertTrue(ItemParser.parse("  ", 3).isEmpty());
    }

    @Test
    void itemParser_enoughFields_included() {
        List<String[]> items = ItemParser.parse("id1;name1;desc1", 3);
        assertEquals(1, items.size());
        assertArrayEquals(new String[]{"id1", "name1", "desc1"}, items.get(0));
    }

    @Test
    void itemParser_tooFewFields_excluded() {
        assertTrue(ItemParser.parse("id1;name1", 3).isEmpty());
    }

    @Test
    void itemParser_exactlyMinFields_included() {
        assertEquals(1, ItemParser.parse("a;b", 2).size());
    }

    @Test
    void itemParser_multipleTokensMixed_onlyValidIncluded() {
        assertEquals(2, ItemParser.parse("a;b;c|x|d;e;f", 3).size());
    }

    @Test
    void itemParser_minFieldsZero_allIncluded() {
        assertEquals(3, ItemParser.parse("a|b|c", 0).size());
    }

    // ==================================================
    //  UserParser
    // ==================================================

    @Test
    void userParser_null_returnsNull() {
        assertNull(UserParser.parse(null));
    }

    @Test
    void userParser_tooFewFields_returnsNull() {
        assertNull(UserParser.parse("id|name|user"));
    }

    @Test
    void userParser_minimalFields_parsedCorrectly() {
        User u = UserParser.parse("uid1|Alice Smith|alice|alice@mail.com");
        assertNotNull(u);
        assertEquals("uid1", u.getUser_id());
        assertEquals("Alice Smith", u.getFullname());
        assertEquals("alice", u.getUsername());
        assertEquals("alice@mail.com", u.getEmail());
    }

    @Test
    void userParser_withDob_parsedCorrectly() {
        User u = UserParser.parse("uid1|Alice|alice|alice@mail.com|1990-01-15");
        assertNotNull(u);
        assertEquals("1990-01-15", u.getDob());
    }

    @Test
    void userParser_emptyDob_dobNotSet() {
        User u = UserParser.parse("uid1|Alice|alice|alice@mail.com|");
        assertNotNull(u);
        assertNull(u.getDob());
    }

    @Test
    void userParser_withRole_parsedCorrectly() {
        User u = UserParser.parse("uid1|Alice|alice|alice@mail.com||ADMIN");
        assertNotNull(u);
        assertEquals(Role.ADMIN, u.getRole());
    }

    @Test
    void userParser_invalidRole_throwsException() {
        assertNull(UserParser.parse("uid1|Alice|alice|alice@mail.com||SUPERUSER"));
    }

    @Test
    void userParser_withBalance_parsedCorrectly() {
        User u = UserParser.parse("uid1|Alice|alice|alice@mail.com||BIDDER|9999.50");
        assertNotNull(u);
        assertEquals(0, new BigDecimal("9999.50").compareTo(u.getBalance()));
    }

    @Test
    void userParser_invalidBalance_returnsNull() {
        assertNull(UserParser.parse("uid1|Alice|alice|alice@mail.com||BIDDER|NOT_A_NUMBER"));
    }

    @Test
    void userParser_emptyBalance_balanceNotSet() {
        User u = UserParser.parse("uid1|Alice|alice|alice@mail.com||BIDDER|");
        assertNotNull(u);
        assertNull(u.getBalance());
    }
}