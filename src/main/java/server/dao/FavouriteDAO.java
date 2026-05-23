package server.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavouriteDAO {

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
            System.out.println("Favourite already exists");
        } catch (Exception e) {
            System.out.println(
                    "FAV ERROR = " + e.getMessage()
            );
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }

        return itemIds;
    }
}