package radon.engine.util;


public class EnumUtils {

    public static <T extends Enum<T>> String[] getValues(Class<T> enumType) {
        String[] enumValues = new String[enumType.getEnumConstants().length];
        int i = 0;
        for (T t : enumType.getEnumConstants()) {
            enumValues[i] = t.name();
            i++;
        }

        return enumValues;
    }

    public static int getIndex(String str, String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (str.equals(array[i])) return i;
        }

        return -1;
    }

}