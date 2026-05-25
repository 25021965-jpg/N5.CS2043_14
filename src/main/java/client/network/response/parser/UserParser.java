package client.network.response.parser;

import model.User;

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

            return u;

        } catch (Exception e) {
            return null;
        }
    }
}