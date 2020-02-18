package ai.gotit.giap;

import android.app.Activity;

import org.json.JSONObject;

import ai.gotit.giap.entity.Event;
import ai.gotit.giap.exception.GIAPInstanceExistsException;
import ai.gotit.giap.service.ConfigManager;
import ai.gotit.giap.service.IdentityManager;
import ai.gotit.giap.service.NetworkManager;
import ai.gotit.giap.service.Repository;
import ai.gotit.giap.service.TaskManager;

public class GIAP {

    private static GIAP instance = null;

    public static GIAP getInstance() {
        return instance;
    }

    private GIAP() {
    }

    public static GIAP initialize(String serverUrl, String token, Activity context) {
        if (instance != null) {
            throw new GIAPInstanceExistsException();
        }

        ConfigManager configManager = ConfigManager.getInstance();
        configManager.setContext(context);
        configManager.setServerUrl(serverUrl);
        configManager.setToken(token);

        Repository.initialize();
        NetworkManager.initialize();
        IdentityManager.initialize();
        TaskManager.initialize();

        instance = new GIAP();
        return instance;
    }

    public void track(String eventName) {
        track(eventName, null);
    }

    public void track(String eventName, JSONObject customProps) {
        Event event;
        if (customProps == null) {
            event = new Event(eventName);
        } else {
            event = new Event(eventName, customProps);
        }
        TaskManager.getInstance().createEventTask(event);
    }

    public void alias(String userId) {
        TaskManager.getInstance().createAliasTask(userId);
    }

    public void identify(String userId) {
        TaskManager.getInstance().createIdentifyTask(userId);
    }

    public void updateProfile(JSONObject props) {
        TaskManager.getInstance().createUpdateProfileTask(props);
    }

    public void reset() {
        IdentityManager.getInstance().generateNewDistinctId();
    }
}
