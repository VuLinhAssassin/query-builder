package com.vulinh.util;

/**
 * Some 'independent' string utility methods so this library can work without any third party StringUtils library.
 *
 * @author Nguyen Vu Linh
 */
public final class StringUtils {

    public static final String SPACE             = " ";
    public static final String EMPTY             = "";
    public static final String OPEN_PARENTHESIS  = "(";
    public static final String CLOSE_PARENTHESIS = ")";
    public static final String COLON             = ":";
    public static final String DOT               = ".";
    public static final String SPACED_AND        = " and ";
    public static final String SPACED_OR         = " or ";
    public static final String FROM_VALUE        = "fromValue";
    public static final String TO_VALUE          = "toValue";

    private StringUtils() {
        throw new UnsupportedOperationException("Cannot instantiate utility class!");
    }

    /**
     * Check if provided data is not blank. Said data, if contains space only, will be considered blank.
     *
     * @param data The provided <code>CharSequence</code> data to check.
     * @return <code>true</code> if the data is not blank; <code>false</code> if otherwise.
     */
    public static boolean isNotBlank(CharSequence data) {
        return !isBlank(data);
    }

    /**
     * Check if provided data is blank. Said data, if contains space only, will be considered blank.
     *
     * @param data The provided <code>CharSequence</code> data to check.
     * @return <code>true</code> if the data is blank; <code>false</code> if otherwise.
     */
    public static boolean isBlank(CharSequence data) {
        int length = length(data);

        if (length == 0) {
            return true;
        }

        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(data.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private static int length(CharSequence data) {
        return data == null ? 0 : data.length();
    }
}