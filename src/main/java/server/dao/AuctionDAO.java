package server.dao;

import model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuctionDAO {

    private static Auction mapAuction(ResultSet rs, Connection conn)
            throws SQLException {

        Auction auction = new Auction();

        auction.setAuction_id(rs.getString("auction_id"));
        auction.setStartingPrice(rs.getBigDecimal("starting_price"));
        auction.setCurrentPrice(rs.getBigDecimal("current_price"));
        auction.setMinIncrement(rs.getBigDecimal("min_increment"));
        auction.setCancelled(rs.getBoolean("is_cancelled"));
        auction.setApproved(rs.getBoolean("is_approved"));

        Timestamp start = rs.getTimestamp("start_time");
        Timestamp end = rs.getTimestamp("end_time");

        if (start != null) auction.setStartTime(start.toLocalDateTime());
        if (end != null) auction.setEndTime(end.toLocalDateTime());

        User seller = new User();
        seller.setUser_id(rs.getString("seller_id"));
        seller.setUsername(rs.getString("seller_name"));
        seller.setFullname(rs.getString("seller_fullname"));
        auction.setSeller(seller);

        Item item = new Item();
        item.setItem_id(rs.getString("item_id"));
        item.setName(rs.getString("item_name"));
        item.setDescription(rs.getString("item_desc"));

        String category = rs.getString("category");

        item.setCategory(
                category != null
                        ? Category.valueOf(category.toUpperCase())
                        : Category.OTHER
        );

        item.setImages(getItemImages(conn, item.getItem_id()));

        auction.setItem(item);

        return auction;
    }

    public static AuctionStatus calculateStatus(
            boolean cancelled,
            boolean approved,
            LocalDateTime start,
            LocalDateTime end
    ) {

        LocalDateTime now =
                LocalDateTime.now();

        if (cancelled)
            return AuctionStatus.CANCELLED;

        if (!approved)
            return AuctionStatus.PENDING_APPROVAL;

        if (
                start != null &&
                        now.isBefore(start)
        )
            return AuctionStatus.UPCOMING;

        if (
                end != null &&
                        now.isAfter(end)
        )
            return AuctionStatus.ENDED;

        return AuctionStatus.ACTIVE;
    }

    public static List<Auction> findAll() {

        List<Auction> auctions = new ArrayList<>();

        String sql = """
                SELECT a.*, i.name AS item_name,
                i.description AS item_desc,
                i.category,
                u.username AS seller_name,
                u.fullname AS seller_fullname
                FROM auctions a
                LEFT JOIN items i ON a.item_id = i.item_id
                LEFT JOIN users u ON a.seller_id = u.user_id
                WHERE a.is_approved = TRUE
                AND a.is_cancelled = FALSE
                """;

        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next())
                auctions.add(mapAuction(rs, conn));

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return auctions;
    }

    public static List<Auction> findPending() {

        List<Auction> auctions = new ArrayList<>();

        String sql = """
                SELECT a.*, i.name AS item_name,
                i.description AS item_desc,
                i.category,
                u.username AS seller_name,
                u.fullname AS seller_fullname
                FROM auctions a
                LEFT JOIN items i ON a.item_id = i.item_id
                LEFT JOIN users u ON a.seller_id = u.user_id
                WHERE a.is_approved = FALSE
                AND a.is_cancelled = FALSE
                """;

        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next())
                auctions.add(mapAuction(rs, conn));

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return auctions;
    }

    public static boolean approveAuction(String auctionId) {

        String sql = """
                UPDATE auctions
                SET is_approved = TRUE
                WHERE auction_id = ?
                """;

        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, auctionId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            return false;
        }
    }

    public static void save(Auction auction) {

        if (
                auction == null ||
                        auction.getItem() == null ||
                        auction.getSeller() == null
        ) return;

        String sql =
                """
                INSERT INTO auctions
                (
                    auction_id,
                    item_id,
                    seller_id,
                    starting_price,
                    current_price,
                    min_increment,
                    start_time,
                    end_time,
                    is_cancelled,
                    is_approved
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection conn =
                        DatabaseService.getConnection()
        ) {

            conn.setAutoCommit(false);

            try {

                saveItem(
                        conn,
                        auction.getItem()
                );

                PreparedStatement ps =
                        conn.prepareStatement(sql);

                ps.setString(
                        1,
                        auction.getAuction_id()
                );

                ps.setString(
                        2,
                        auction.getItem().getItem_id()
                );

                ps.setString(
                        3,
                        auction.getSeller().getUser_id()
                );

                ps.setBigDecimal(
                        4,
                        auction.getStartingPrice()
                );

                ps.setBigDecimal(
                        5,
                        auction.getCurrentPrice()
                );

                ps.setBigDecimal(
                        6,
                        auction.getMinIncrement()
                );

                ps.setTimestamp(
                        7,
                        Timestamp.valueOf(
                                auction.getStartTime()
                        )
                );

                ps.setTimestamp(
                        8,
                        Timestamp.valueOf(
                                auction.getEndTime()
                        )
                );

                ps.setBoolean(
                        9,
                        false
                );

                ps.setBoolean(
                        10,
                        false
                ); // mặc định pending approval

                ps.executeUpdate();

                conn.commit();

            }

            catch (Exception e) {

                conn.rollback();

                throw e;
            }

        }

        catch (Exception e) {

            System.out.println(
                    "Save auction error: "
                            + e.getMessage()
            );

            throw new RuntimeException(
                    e.getMessage()
            );
        }
    }

    public static List<Auction> findBySeller(String sellerId) {

        List<Auction> auctions = new ArrayList<>();

        String sql = """
                SELECT a.*, i.name AS item_name,
                i.description AS item_desc,
                i.category,
                u.username AS seller_name,
                u.fullname AS seller_fullname
                FROM auctions a
                LEFT JOIN items i ON a.item_id = i.item_id
                LEFT JOIN users u ON a.seller_id = u.user_id
                WHERE a.seller_id = ?
                ORDER BY a.start_time DESC
                """;

        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, sellerId);

            ResultSet rs = ps.executeQuery();

            while (rs.next())
                auctions.add(mapAuction(rs, conn));

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return auctions;
    }

    public static List<Auction> findJoinedAuctions(String userId) {

        List<Auction> auctions = new ArrayList<>();

        String sql = """
                SELECT DISTINCT a.*, i.name AS item_name,
                i.description AS item_desc,
                i.category,
                u.username AS seller_name,
                u.fullname AS seller_fullname
                FROM bids b
                JOIN auctions a ON b.auction_id = a.auction_id
                LEFT JOIN items i ON a.item_id = i.item_id
                LEFT JOIN users u ON a.seller_id = u.user_id
                WHERE b.bidder_id = ?
                ORDER BY a.start_time DESC
                """;

        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next())
                auctions.add(mapAuction(rs, conn));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return auctions;
    }

    public static void cancelAuction(String id) {

        String sql = """
                UPDATE auctions
                SET is_cancelled = TRUE
                WHERE auction_id = ?
                """;

        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static boolean stopAuction(String id) {

        String sql = """
                UPDATE auctions
                SET end_time = NOW()
                WHERE auction_id = ?
                """;

        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean resumeAuction(String id) {

        String sql = """
                UPDATE auctions
                SET is_cancelled = FALSE
                WHERE auction_id = ?
                """;

        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            return false;
        }
    }

    public static List<Bid> getBidHistory(
            String auctionId
    ) {

        List<Bid> history =
                new ArrayList<>();

        String sql =
                """
                SELECT
                b.bid_amount,
                b.bid_time,
                u.username
    
                FROM bids b
    
                JOIN users u
                ON b.bidder_id = u.user_id
    
                WHERE b.auction_id = ?
    
                ORDER BY b.bid_amount ASC
                """;

        try (

                Connection conn =
                        DatabaseService.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)

        ) {

            ps.setString(
                    1,
                    auctionId
            );

            ResultSet rs =
                    ps.executeQuery();

            while (rs.next()) {

                Bid bid =
                        new Bid();

                bid.setAmount(
                        rs.getBigDecimal(
                                "bid_amount"
                        )
                );

                bid.setTime(
                        rs.getTimestamp(
                                "bid_time"
                        ).toLocalDateTime()
                );

                bid.setUsername(
                        rs.getString(
                                "username"
                        )
                );

                history.add(
                        bid
                );
            }

        }

        catch (SQLException e) {

            System.out.println(
                    e.getMessage()
            );
        }

        return history;
    }

    public static List<String[]>
    findAuctionHistory() {

        List<String[]> result =
                new ArrayList<>();

        String sql =
                """
                SELECT
                a.auction_id,
    
                i.name AS item_name,
    
                a.current_price,
    
                a.end_time,
    
                a.is_cancelled
    
                FROM auctions a
    
                LEFT JOIN items i
                ON a.item_id = i.item_id
    
                WHERE
                a.is_cancelled = TRUE
    
                OR NOW() > a.end_time
                """;

        try (

                Connection conn =
                        DatabaseService.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()

        ) {

            while (rs.next()) {

                String status =
                        rs.getBoolean(
                                "is_cancelled"
                        )

                                ? "CANCELLED"

                                : "ENDED";

                result.add(

                        new String[] {

                                rs.getString(
                                        "auction_id"
                                ),

                                rs.getString(
                                        "item_name"
                                ),

                                rs.getBigDecimal(
                                        "current_price"
                                ).toPlainString(),

                                rs.getTimestamp(
                                        "end_time"
                                ).toString(),

                                status
                        }
                );
            }

        }

        catch (SQLException e) {

            System.out.println(
                    e.getMessage()
            );
        }

        return result;
    }

    public static List<String[]>
    findAllAsStrings() {

        List<String[]> result =
                new ArrayList<>();

        String sql =
                """
                SELECT
    
                a.auction_id,
    
                i.name item_name,
    
                u.username seller,
    
                a.current_price,
    
                a.is_approved,
    
                a.is_cancelled,
    
                a.start_time,
    
                a.end_time
    
                FROM auctions a
    
                LEFT JOIN items i
                ON a.item_id=i.item_id
    
                LEFT JOIN users u
                ON a.seller_id=u.user_id
                """;

        try (

                Connection conn =
                        DatabaseService.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()

        ) {

            while (rs.next()) {

                AuctionStatus status =
                        calculateStatus(

                                rs.getBoolean(
                                        "is_cancelled"
                                ),

                                rs.getBoolean(
                                        "is_approved"
                                ),

                                rs.getTimestamp(
                                        "start_time"
                                ).toLocalDateTime(),

                                rs.getTimestamp(
                                        "end_time"
                                ).toLocalDateTime()
                        );

                result.add(

                        new String[] {

                                rs.getString(
                                        "auction_id"
                                ),

                                rs.getString(
                                        "item_name"
                                ),

                                rs.getString(
                                        "seller"
                                ),

                                rs.getBigDecimal(
                                        "current_price"
                                ).toPlainString(),

                                status.name()
                        }
                );
            }

        }

        catch (SQLException e) {

            System.out.println(
                    e.getMessage()
            );
        }

        return result;
    }

    private static void saveItem(Connection conn, Item item) {

        if (item == null) return;

        String sql = """
                INSERT IGNORE INTO items
                (item_id,name,description,category)
                VALUES (?,?,?,?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getItem_id());
            ps.setString(2, item.getName());
            ps.setString(3, item.getDescription());
            ps.setString(4, item.getCategory().name());

            ps.executeUpdate();

            if (item.getImages() != null)
                for (String url : item.getImages())
                    saveItemImage(conn, item.getItem_id(), url);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void saveItemImage(
            Connection conn,
            String itemId,
            String url
    ) {

        String sql = """
                INSERT IGNORE INTO item_images
                (image_id,item_id,image_url)
                VALUES (?,?,?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, itemId);
            ps.setString(3, url);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static List<String> getItemImages(
            Connection conn,
            String itemId
    ) {

        List<String> images = new ArrayList<>();

        String sql = """
                SELECT image_url
                FROM item_images
                WHERE item_id = ?
                ORDER BY created_at ASC
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemId);

            ResultSet rs = ps.executeQuery();

            while (rs.next())
                images.add(rs.getString("image_url"));

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return images;
    }
}