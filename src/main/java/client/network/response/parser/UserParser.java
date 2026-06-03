package client.network.response.parser;

import model.Entity.User.Role;
import model.Entity.User.User;
import model.Factory.UserFactory;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserParser {
    private static final Logger LOGGER =
            Logger.getLogger(UserParser.class.getName());

    public static User parse(String data) {
        try {

            String[] p = data.split("\\|", -1);

            if (p.length < 4) return null;

            Role role = Role.BIDDER;

            if (p.length > 5 && !p[5].isEmpty()) {
                role = Role.valueOf(p[5].trim());
            }

            User u = UserFactory.create(role);

            u.setUser_id(p[0]);
            u.setFullname(p[1]);
            u.setUsername(p[2]);
            u.setEmail(p[3]);

            // dob
            if (p.length > 4 && !p[4].isEmpty()) {
                u.setDob(p[4]);
            }

            // role
            if (p.length > 5 && !p[5].isEmpty()) {
                u.setRole(Role.valueOf(p[5].trim()));
            }

            // balance
            if (p.length > 6 && !p[6].isEmpty()) {
                u.setBalance(new BigDecimal(p[6]));
            }

            return u;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
            return null;
        }
    }
}