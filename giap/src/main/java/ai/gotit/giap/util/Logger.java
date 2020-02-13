package ai.gotit.giap.util;

import android.util.Log;

import ai.gotit.giap.constant.CommonConstant;

public class Logger {
    public static void warn(Exception exception, String message) {
        Log.w(CommonConstant.LOG_TAG, message, exception);
    }

    public static void warn(Exception exception) {
        warn(exception, "");
    }

    public static void error(Exception exception, String message) {
        Log.e(CommonConstant.LOG_TAG, message, exception);
    }

    public static void error(Exception exception) {
        error(exception, "");
    }
}
