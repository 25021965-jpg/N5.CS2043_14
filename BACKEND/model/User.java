package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class User implements Serializable {
    private String id;
    private String username;
    private String email;
    private String password;
    private List<Role> roles;
    private BigDecimal balance;
    private boolean verified;

    public User(String id, String username, String email, String password, List<Role> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.balance = BigDecimal.ZERO;
        this.verified = true;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public boolean isVerified() {
        return verified;
    }

    public boolean hasRole(Role role) {
        return roles != null && roles.contains(role);
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}