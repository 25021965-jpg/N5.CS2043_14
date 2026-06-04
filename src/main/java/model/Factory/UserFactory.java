package model.Factory;

import model.Entity.User.*;

public class UserFactory {

    public static User create(Role role) {
        if (role == null) {
            return new Bidder();
        }

        return switch (role) {
            case ADMIN  -> new Admin();
            case SELLER -> new Seller();
            case BIDDER -> new Bidder();
        };
    }

    public static User createFromRole(String roleStr) {
        if (roleStr == null) {
            return new Bidder();
        }

        try {
            return create(Role.valueOf(roleStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return new Bidder();
        }
    }
}