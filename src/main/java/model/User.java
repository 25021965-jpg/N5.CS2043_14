package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String user_id;
    private String username;
    private String password;
    private String fullname;
    private String email;
    private String dob;
    private BigDecimal balance;
    private boolean verified;
    private List<String> transactions = new ArrayList<>();

    private Role role = Role.BIDDER; // Mặc định là người mua

    public User() {}

    // Getter và Setter cho Role
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Hàm để check quyền
    public boolean hasRole(Role requiredRole) {
        return this.role == requiredRole || this.role == Role.ADMIN;
    }

    // Getter/setter khác
    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public List<String> getTransactions() {return transactions;}
    public void setTransactions(List<String> transactions) {this.transactions = transactions;}
    public void addTransaction(String transaction) {
        transactions.add(transaction);
    }


}