package server.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FavouriteDAO {

    private static final Logger LOGGER =
            Logger.getLogger(FavouriteDAO.class.getName());

    // ==================== ADD FAVOURITE ====================
    public static boolean addFavourite(String userId, String itemId) {
        String sql = """
                INSERT INTO favourites(user_id, item_id)
                VALUES (?, ?)
                """;
        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, userId);
            stmt.setString(2, itemId);

            return stmt.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            LOGGER.warning(
                    "Favourite already exists. userId="
                            + userId
                            + ", itemId="
                            + itemId
            );

        } catch (Exception e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Failed to add favourite. userId="
                            + userId
                            + ", itemId="
                            + itemId,
                    e
            );
        }
        return false;
    }

    // ==================== REMOVE FAVOURITE ====================
    public static boolean removeFavourite(String userId, String itemId) {
        String sql = """
                DELETE FROM favourites
                WHERE user_id = ? AND item_id = ?
                """;
        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, userId);
            stmt.setString(2, itemId);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Failed to remove favourite. userId="
                            + userId
                            + ", itemId="
                            + itemId,
                    e
            );
        }
        return false;
    }

    // ==================== CHECK FAVOURITE ====================
    public static boolean isFavourite(String userId, String itemId) {
        String sql = """
                SELECT *
                FROM favourites
                WHERE user_id = ? AND item_id = ?
                """;
        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, userId);
            stmt.setString(2, itemId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Failed to check favourite. userId="
                            + userId
                            + ", itemId="
                            + itemId,
                    e
            );
        }
        return false;
    }

    // ==================== GET USER FAVOURITES ====================
    public static List<String> getFavouriteItemIds(String userId) {
        List<String> itemIds = new ArrayList<>();
        String sql = """
                SELECT item_id
                FROM favourites
                WHERE user_id = ?
                """;
        try (
                Connection conn = DatabaseService.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                itemIds.add(rs.getString("item_id"));
            }

        } catch (Exception e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Failed to get favourites. userId="
                            + userId,
                    e
            );
        }
        return itemIds;
    }
}