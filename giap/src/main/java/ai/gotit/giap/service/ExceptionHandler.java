package ai.gotit.giap.service;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final int SLEEP_TIMEOUT_MS = 400;
    private static ExceptionHandler instance;
    private final Thread.UncaughtExceptionHandler defaultExceptionHandler;

    private ExceptionHandler() {
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static void initialize() {
        if (instance == null) {
            synchronized (ExceptionHandler.class) {
                if (instance == null) {
                    instance = new ExceptionHandler();
                }
            }
        }
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        TaskManager taskManager = TaskManager.getInstance();
        if (taskManager != null) {
            taskManager.stop();
        }

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
        System.exit(10);
    }
}
