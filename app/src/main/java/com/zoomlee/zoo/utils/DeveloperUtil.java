package com.zoomlee.zoo.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 1/13/15
 */
public class DeveloperUtil {


    public static void michaelLog() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        String methodName = elements[3].getMethodName();
        michaelLog(methodName);
    }

    public static void michaelLog(Object toLog) {
        //TODO:
        //if (!BuildConfig.DEBUG)
         //   return;
        if (toLog == null)
            Log.e("Michael", "Try to print NULL object");
        else if (toLog instanceof Object[]) {
            Object[] array = (Object[]) toLog;
            Log.e("Michael", "" + toLog.getClass() + " [");
            for (Object o : array) {
                if (o == null)
                    Log.e("Michael", "NULL item");
                else
                    Log.e("Michael", o.toString());
            }
            Log.e("Michael", " ]");
        } else
            Log.e("Michael", toLog.toString());
    }

    public static String inputStream2String(InputStream is) {
        StringBuilder inputStringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            String line = bufferedReader.readLine();
            while (line != null) {
                inputStringBuilder.append(line);
                inputStringBuilder.append('\n');
                line = bufferedReader.readLine();
            }
        } catch (IOException ioe) {
            return "Can't read input stream\nERROR - " + ioe.getMessage();
        }

        return inputStringBuilder.toString();
    }

    public static void inputStream2Log(InputStream is) {
        michaelLog(inputStream2String(is));
    }
}
