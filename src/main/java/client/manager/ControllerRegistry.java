package client.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ControllerRegistry {

    private static final Map<Class<?>, Object> controllers = new ConcurrentHashMap<>();

    public static <T> void register(Class<T> clazz, T controller) {
        controllers.put(clazz, controller);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz) {
        return (T) controllers.get(clazz);
    }
}