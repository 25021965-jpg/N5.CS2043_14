package model.Entity.User;

/**
 * Quản trị viên — có toàn quyền quản lý hệ thống,
 * duyệt/hủy phiên đấu giá, quản lý người dùng.
 */
public class Admin extends User {

    public Admin() {
        setRole(Role.ADMIN);
    }

    @Override
    public String getDefaultAction() {
        return "Manage system";
    }

    @Override
    public String getSummary() {
        return "[ADMIN] " + getUsername();
    }
}