package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class User implements Serializable {

    private String id;
    private String fullname;
    private String username;
    private String email;
    private String dob;
    private String password;
    private List<Role> roles;
    private BigDecimal balance;
    private boolean verified;

    public User(
            String id,
            String fullname,
            String username,
            String email,
            String password,
            List<Role> roles
    ) {

        this.id = id;
        this.fullname = fullname;
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;

        this.balance = BigDecimal.ZERO;
        this.verified = true;
    }

    public User() {
    }

    public boolean hasRole(Role role) {

        return roles != null
                && roles.contains(role);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}