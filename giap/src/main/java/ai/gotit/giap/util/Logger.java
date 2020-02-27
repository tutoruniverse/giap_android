package ai.gotit.giap.util;

import android.util.Log;

import java.util.Date;

import ai.gotit.giap.constant.CommonConstant;

public class Logger {
    private static String constructMessage(String message) {
        String time = String.valueOf(new Date().getTime());
        return time + " - " + message;
    }

    public static void log(String message) {
        Log.d(CommonConstant.LOG_TAG, constructMessage(message));
    }

    public static void warn(Throwable exception, String message) {
        Log.w(CommonConstant.LOG_TAG, constructMessage(message), exception);
    }

    public static void warn(Throwable exception) {
        warn(exception, "Warning");
    }

    public static void warn(String message) {
        Log.w(CommonConstant.LOG_TAG, constructMessage(message));
    }

    public static void error(Throwable exception, String message) {
        Log.e(CommonConstant.LOG_TAG, constructMessage(message), exception);
    }

    public static void error(Throwable exception) {
        error(exception, "Error");
    }

    public static void error(String message) {
        Log.e(CommonConstant.LOG_TAG, constructMessage(message));
    }
}
