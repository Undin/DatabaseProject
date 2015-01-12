package com.warrior.databaseproject;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by warrior on 12.01.15.
 */
public class Utils {

    private static final String DELIMITER = ", ";

    public static String wrap(String str) {
        return "'" + str + "'";
    }

    public static String toSqlString(String str) {
        if (str.contains("'")) {
            return "E" + wrap(str.replaceAll("'", "\\\\'"));
        }
        return wrap(str);
    }

    public static String toString(Object... objects) {
        return "(" + Arrays.stream(objects).map(o -> {
            if (o instanceof String) {
                return toSqlString((String) o);
            }
            return String.valueOf(o);
        }).collect(Collectors.joining(DELIMITER)) + ")";
    }

}
