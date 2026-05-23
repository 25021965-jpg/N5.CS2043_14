package client.manager;

import java.util.HashMap;
import java.util.Map;

public class ControllerRegistry {

    private static final Map<Class<?>, Object> controllers = new HashMap<>();

    public static <T> void register(Class<T> clazz, T controller) {
        controllers.put(clazz, controller);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz) {
        return (T) controllers.get(clazz);
    }
}