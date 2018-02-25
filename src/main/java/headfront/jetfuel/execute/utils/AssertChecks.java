package headfront.jetfuel.execute.utils;


/**
 * Created by Deepak on 25/02/2018.
 */
public class AssertChecks {

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
    public static void notNull(Object object) {
        notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    public static void hasLength(String text, String message) {
        if (!hasLength(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static boolean hasLength(CharSequence str) {
        return (str != null && str.length() > 0);
    }
}
