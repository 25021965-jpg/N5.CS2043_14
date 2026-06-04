package model.Entity.User;

/**
 * Người bán — có thể tạo phiên đấu giá và quản lý sản phẩm.
 */
public class Seller extends User {

    public Seller() {
        setRole(Role.SELLER);
    }

    @Override
    public String getDefaultAction() {
        return "Create auction";
    }

    @Override
    public String getSummary() {
        return "[SELLER] " + getUsername();
    }
}