import java.util.*;

class User {
    String username;
    String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

class AuthSystem {
    private List<User> users = new ArrayList<>();
    private User currentUser = null;

    public AuthSystem() {
        // user mẫu
        users.add(new User("admin", "123"));
        users.add(new User("user", "456"));
    }

    public boolean login(String username, String password) {
        for (User u : users) {
            if (u.username.equals(username) && u.password.equals(password)) {
                currentUser = u;
                return true;
            }
        }
        return false;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getCurrentUser() {
        return currentUser != null ? currentUser.username : "No user";
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        AuthSystem auth = new AuthSystem();

        while (true) {
            System.out.println("\n1. Login");
            System.out.println("2. Logout");
            System.out.println("3. Check user");
            System.out.println("0. Exit");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Username: ");
                    String u = sc.nextLine();
                    System.out.print("Password: ");
                    String p = sc.nextLine();

                    if (auth.login(u, p)) {
                        System.out.println("Login success!");
                    } else {
                        System.out.println("Login failed!");
                    }
                    break;

                case 2:
                    auth.logout();
                    System.out.println("Logged out!");
                    break;

                case 3:
                    System.out.println("Current user: " + auth.getCurrentUser());
                    break;

                case 0:
                    return;
            }
        }
    }
}