package ai.gotit.giap;

import java.lang.reflect.Field;

public class TestHelper {
    public static void resetSingleton(Class clazz) {
        Field instance;
        try {
            instance = clazz.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
