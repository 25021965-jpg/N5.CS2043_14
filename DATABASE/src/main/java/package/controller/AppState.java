package com.yourpackage.utils;

import com.yourpackage.model.User;

public class AppState {
    private static User currentUser = null;

    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User user) { currentUser = user; }
    public static boolean isLoggedIn() { return currentUser != null; }
    public static void logout() { currentUser = null; }
}
