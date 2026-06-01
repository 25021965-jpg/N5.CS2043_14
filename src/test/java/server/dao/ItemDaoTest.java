package server.dao;

import model.Item;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ItemDAOTest extends TiDBRollbackBase {

    @Test
    void testFullItemLifecycle() throws Exception {
        // 1. Phủ saveItem (bao gồm cả trường hợp Category null và có Images)
        Item item = new Item();
        String iId = generateId("itm");
        item.setItem_id(iId);
        item.setName("Test Item");
        item.setDescription("Test Desc");
        item.setCategory(null); // Phủ nhánh Category == null -> "OTHER"

        List<String> images = new ArrayList<>();
        images.add("http://img1.png");
        images.add(""); // Phủ nhánh !url.isBlank()
        item.setImages(images);

        ItemDAO.saveItem(conn, item);
        createdItemIds.add(iId);

        // 2. Phủ getItemImages
        List<String> savedImages = ItemDAO.getItemImages(conn, iId);
        assertFalse(savedImages.isEmpty());

        // 3. Phủ updateItem
        assertTrue(ItemDAO.updateItem(iId, "New Name", "New Desc", "JEWELRY"));

        // 4. Phủ findAllWithSeller (Bao gồm cả JOIN và CASE status)
        // Tạo thêm Auction để Status không bị NULL/DEFAULT
        insertUser("sel-1", "u-item", "u@mail.com", "123", "SELLER", 0);
        insertAuction("auc-itm", iId, "sel-1", 100, true, false, "2020-01-01 00:00:00", "2099-01-01 00:00:00");
        createdAuctionIds.add("auc-itm");
        createdUserIds.add("sel-1");

        List<String[]> list = ItemDAO.findAllWithSeller();
        assertFalse(list.isEmpty());

        // 5. Phủ deleteItem
        assertTrue(ItemDAO.deleteItem(iId));
    }

    @Test
    void testItemEdgeCases() throws Exception {
        // Phủ các nhánh return sớm và lỗi SQL
        ItemDAO.saveItem(conn, null);
        ItemDAO.saveItem(conn, new Item());

        // Phủ delete/update với ID không tồn tại
        assertFalse(ItemDAO.deleteItem("non-existent"));
        assertFalse(ItemDAO.updateItem("non-exist", "n", "d", "c"));
    }
}