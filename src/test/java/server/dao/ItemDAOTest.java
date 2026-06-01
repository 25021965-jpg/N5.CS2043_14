package server.dao;

import model.Item;
import model.Category;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ItemDAOTest extends TiDBRollbackBase {

    @Test
    void testFullItemLifecycle() throws Exception {
        // Tạo ID ngẫu nhiên để tránh trùng khi chạy lại
        String iId = generateId("itm");

        // Tạo item với category null → nhánh fallback "OTHER" trong saveItem
        Item item = new Item();
        item.setItem_id(iId);
        item.setName("Test Item");
        item.setDescription("Test Desc");
        item.setCategory(null);

        // Thêm 1 url hợp lệ và 1 url blank để kiểm tra nhánh lọc url trong saveItemImage
        List<String> images = new ArrayList<>();
        images.add("http://img1.png");
        images.add(""); // url blank → bị bỏ qua, không lưu
        item.setImages(images);

        ItemDAO.saveItem(conn, item);

        // Chỉ url không blank mới được lưu → kết quả phải là 1 ảnh
        List<String> savedImages = ItemDAO.getItemImages(conn, iId);
        assertEquals(1, savedImages.size());
        assertEquals("http://img1.png", savedImages.get(0));

        // Cập nhật thông tin item
        assertTrue(ItemDAO.updateItem(iId, "New Name", "New Desc", "JEWELRY"));

        // Tạo thêm auction để findAllWithSeller có dữ liệu status
        insertUser("sel-itm", "u-item-" + iId, "u" + iId + "@mail.com", "123", "SELLER", 0);
        insertAuction("auc-itm-" + iId, iId, "sel-itm", 100, true, false,
                "2020-01-01 00:00:00", "2099-01-01 00:00:00");

        // Kiểm tra item xuất hiện trong danh sách với thông tin seller
        List<String[]> list = ItemDAO.findAllWithSeller();
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(row -> iId.equals(row[0])));

        // Xóa item → không còn trong danh sách
        assertTrue(ItemDAO.deleteItem(iId));
        assertFalse(ItemDAO.findAllWithSeller().stream().anyMatch(row -> iId.equals(row[0])));
    }

    @Test
    void testItemEdgeCases() {
        // Truyền null hoặc item không có id → return sớm, không throw
        assertDoesNotThrow(() -> ItemDAO.saveItem(conn, null));
        assertDoesNotThrow(() -> ItemDAO.saveItem(conn, new Item()));

        // ID không tồn tại → trả về false
        assertFalse(ItemDAO.deleteItem("non-existent-xyz"));
        assertFalse(ItemDAO.updateItem("non-exist-xyz", "n", "d", "c"));
    }

    @Test
    void testSaveItem_withCategory() throws Exception {
        String iId = generateId("itm2");

        // Tạo item với category hợp lệ và images = null → bỏ qua vòng lặp images
        Item item = new Item();
        item.setItem_id(iId);
        item.setName("Watch");
        item.setDescription("Desc");
        item.setCategory(Category.ELECTRONICS);
        item.setImages(null);

        assertDoesNotThrow(() -> ItemDAO.saveItem(conn, item));

        // Xác nhận category được lưu đúng vào DB
        try (var ps = conn.prepareStatement("SELECT category FROM items WHERE item_id = ?")) {
            ps.setString(1, iId);
            var rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals("ELECTRONICS", rs.getString("category"));
        }
    }
}