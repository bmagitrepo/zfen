package com.zoomlee.zoo.utils;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 08.04.15.
 */
public class DBUtil {

    /**
     * Format args to string: "(x,x,x,x)"
     * @param selectionArgs
     * @return formatted string
     */
    public static String formatArgsAsSet(String[] selectionArgs) {
        if (selectionArgs == null || selectionArgs.length == 0) return "()";

        StringBuilder fieldTypes = new StringBuilder();
        fieldTypes.append("(");
        for (int i = 0; i < selectionArgs.length; i++) {
            fieldTypes.append(selectionArgs[i]).append(",");
        }
        fieldTypes.deleteCharAt(fieldTypes.length() - 1);
        fieldTypes.append(")");

        return fieldTypes.toString();
    }

    /**
     * Format args to sql projection string: "x, x, x, x"
     * @param projectionArgs
     * @return formatted string
     */
    public static String formatProjection(String[] projectionArgs) {
        StringBuilder projectionString = new StringBuilder();
        for (int i = 0; i < projectionArgs.length; i++) {
            projectionString.append(projectionArgs[i]);
            if (i != projectionArgs.length - 1)
                projectionString.append(", ");
        }

        return projectionString.toString();
    }

    /**
     *
     * @param args
     * @return array of argument's String representation ( by calling .toString() method)
     */
    public static String[] getArgsArray(Object... args) {
        String[] result = new String[args.length];
        for (int i = 0; i < args.length; i++)
            result[i] = String.valueOf(args[i]);
        return result;
    }
}
