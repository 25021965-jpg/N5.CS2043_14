package client.network.response.parser;

import model.Role;
import model.User;

import java.math.BigDecimal;

public class UserParser {

    public static User parse(String data) {

        try {

            String[] p = data.split("\\|", -1);

            if (p.length < 4) return null;

            User u = new User();

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

            e.printStackTrace();
            return null;
        }
    }
}