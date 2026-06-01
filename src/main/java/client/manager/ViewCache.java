package client.manager;

import javafx.scene.Parent;

import java.util.HashMap;
import java.util.Map;

public class ViewCache {

    private static final Map<String, Parent> cache = new HashMap<>();

    private ViewCache() {
    }

    public static Parent get(String fxmlPath) {
        return cache.get(fxmlPath);
    }

    public static void put(String fxmlPath, Parent root) {
        cache.put(fxmlPath, root);
    }

    public static boolean contains(String fxmlPath) {
        return cache.containsKey(fxmlPath);
    }

    public static void remove(String fxmlPath) {
        cache.remove(fxmlPath);
    }

    public static void clear() {
        cache.clear();
    }
}