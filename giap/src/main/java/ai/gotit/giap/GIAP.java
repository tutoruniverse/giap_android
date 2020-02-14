package ai.gotit.giap;

import android.app.Activity;

import ai.gotit.giap.exception.GIAPInstanceExistsException;
import ai.gotit.giap.service.ConfigManager;
import ai.gotit.giap.service.Repository;
import ai.gotit.giap.service.TaskManager;

public class GIAP {

    private static GIAP instance = null;

    public static GIAP getInstance() {
        return instance;
    }

    private GIAP() {
    }

    public static GIAP initialize(String serverUrl, String token, Activity activity) {
        if (instance != null) {
            throw new GIAPInstanceExistsException();
        }

        ConfigManager configManager = ConfigManager.getInstance();
        configManager.setServerUrl(serverUrl);
        configManager.setToken(token);

        Repository.initialize(activity);

        TaskManager.initialize();

        instance = new GIAP();
        return instance;
    }

}
