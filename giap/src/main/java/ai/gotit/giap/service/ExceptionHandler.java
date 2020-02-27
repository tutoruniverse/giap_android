package ai.gotit.giap.service;

import androidx.annotation.VisibleForTesting;

import ai.gotit.giap.GIAP;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final int SLEEP_TIMEOUT_MS = 400;
    private final Thread.UncaughtExceptionHandler defaultExceptionHandler;
    private GIAP giapInstance;

    public ExceptionHandler(GIAP giapInstance) {
        this.giapInstance = giapInstance;
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static ExceptionHandler makeInstance(GIAP giapInstance) {
        return new ExceptionHandler(giapInstance);
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        giapInstance.onUncaughtException();

        if (defaultExceptionHandler != null) {
            defaultExceptionHandler.uncaughtException(t, e);
        } else {
            killProcessAndExit();
        }
    }

    private void killProcessAndExit() {
        try {
            Thread.sleep(SLEEP_TIMEOUT_MS);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        exit();
    }

    @VisibleForTesting
    void exit() {
        System.exit(10);
    }
}
