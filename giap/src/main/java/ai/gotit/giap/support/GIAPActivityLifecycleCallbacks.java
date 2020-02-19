package ai.gotit.giap.support;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import ai.gotit.giap.service.TaskManager;
import ai.gotit.giap.util.Logger;

public class GIAPActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private boolean isBackground = false;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
        isBackground = true;
        Logger.log("LIFECYCLE: foreground -> background");
        TaskManager taskManager = TaskManager.getInstance();
        if (taskManager != null) {
            taskManager.stop();
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (isBackground) {
            Logger.log("LIFECYCLE: background -> foreground");
            TaskManager taskManager = TaskManager.getInstance();
            if (taskManager != null) {
                taskManager.restart();
            }
        }
        isBackground = false;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Logger.log("LIFECYCLE: stopped");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Logger.log("LIFECYCLE: destroyed");
    }
}
