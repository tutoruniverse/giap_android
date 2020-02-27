package ai.gotit.giap.support;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.VisibleForTesting;

import ai.gotit.giap.GIAP;
import ai.gotit.giap.util.Logger;

public class GIAPActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private boolean isBackground = false;
    private GIAP giapInstance;

    public GIAPActivityLifecycleCallbacks(GIAP giapInstance) {
        this.giapInstance = giapInstance;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Logger.log("LIFECYCLE: created!");
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
        isBackground = true;
        Logger.log("LIFECYCLE: foreground -> background");
        giapInstance.onPause();
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (isBackground) {
            Logger.log("LIFECYCLE: background -> foreground");
            giapInstance.onResume();
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

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    boolean getBackground() {
        return isBackground;
    }
}
