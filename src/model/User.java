package model;

import java.math.BigDecimal;
import java.util.List;

public class User {
    private String id;
    private String username;
    private String email;
    private String password;
    private List<Role> roles;
    private BigDecimal balance;
    private boolean verified;

    public boolean hasRole(Role role) {
        return roles != null && roles.contains(role);
    }

    // Getter & Setter
    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}