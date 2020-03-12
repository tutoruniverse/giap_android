package ai.gotit.giap;

import android.app.Application;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import ai.gotit.giap.entity.Event;
import ai.gotit.giap.exception.GIAPInstanceExistsException;
import ai.gotit.giap.service.ConfigManager;
import ai.gotit.giap.service.DeviceInfoManager;
import ai.gotit.giap.service.ExceptionHandler;
import ai.gotit.giap.service.IdentityManager;
import ai.gotit.giap.service.NetworkManager;
import ai.gotit.giap.service.Storage;
import ai.gotit.giap.service.TaskManager;
import ai.gotit.giap.support.GIAPActivityLifecycleCallbacks;
import ai.gotit.giap.util.Logger;

/**
 * Main class of GIAP Android SDK.
 *
 * <p>Call {@link #initialize(String, String, Context)} first to initialize GIAP and its
 * sub-services.</p>
 *
 * <p>Then call {@link #getInstance()} to get singleton instance. CAUTION: getInstance can return
 * null if {@link #initialize(String, String, Context)} is not called first.</p>
 *
 * <p>Call {@link #track(String, JSONObject)} to emit your event to the system:</p>
 * <pre>
 * {@code
 * final GIAP giap = GIAP.initialize(
 *     "http://server-url.example",
 *     "your_project_token",
 *     MainActivity.this
 * );
 *
 * try {
 *     JSONObject props = new JSONObject();
 *     props.put("price", 100);
 *     props.put("package_sku", "package_1_free");
 *     giap.track("Purchase", props);
 * } catch (JSONException e) {}
 * }
 * </pre>
 */
public class GIAP {

    private static GIAP instance = null;
    private ExceptionHandler exceptionHandler;
    private ConfigManager configManager;
    private Storage storage;
    private DeviceInfoManager deviceInfoManager;
    private NetworkManager networkManager;
    private IdentityManager identityManager;
    private TaskManager taskManager;

    /**
     * @return Instance of GIAP. Should be called after GIAP.initialize()
     */
    public static GIAP getInstance() {
        return instance;
    }

    public GIAP() {
    }

    private GIAP(String serverUrl, String token, Context context) {
        exceptionHandler = ExceptionHandler.makeInstance(GIAP.this);
        configManager = ConfigManager.makeInstance(context, serverUrl, token);
        storage = Storage.makeInstance(configManager);
        deviceInfoManager = DeviceInfoManager.makeInstance(configManager, storage);
        networkManager = NetworkManager.makeInstance(configManager);
        identityManager = IdentityManager.makeInstance(storage);
        taskManager = TaskManager.makeInstance(storage, identityManager, networkManager);

        // Only called after initialized all services
        registerGIAPActivityLifecycleCallbacks(context);
    }

    /**
     * Method to initialize single instance of GIAP and all its sub-services.
     * Should always be call first before interacting with GIAP.
     *
     * @param serverUrl URL of GIAP Platform Core API
     * @param token     Token of your product
     * @param context   Application's context that you want to track
     * @return Instance of GIAP
     */
    public static GIAP initialize(String serverUrl, String token, Context context) {
        if (instance == null) {
            synchronized (GIAP.class) {
                if (instance == null) {
                    instance = new GIAP(serverUrl, token, context);
                    return instance;
                }
            }
        }
        throw new GIAPInstanceExistsException();
    }

    private void registerGIAPActivityLifecycleCallbacks(Context context) {
        if (context.getApplicationContext() instanceof Application) {
            final Application app = (Application) context.getApplicationContext();
            GIAPActivityLifecycleCallbacks callbacks = new GIAPActivityLifecycleCallbacks(GIAP.this);
            app.registerActivityLifecycleCallbacks(callbacks);
        } else {
            Logger.warn("Context is not an Application. We won't be able to automatically flush on background.");
        }
    }

    public void onUncaughtException() {
        if (taskManager != null) {
            taskManager.stop();
        }
    }

    public void onPause() {
        if (taskManager != null) {
            taskManager.stop();
        }
    }

    public void onResume() {
        if (taskManager != null) {
            taskManager.restart();
        }
    }

    /**
     * Method to emit your event, without any custom properties.
     *
     * @param eventName Name of the event you want to track
     */
    public void track(String eventName) {
        track(eventName, null);
    }

    /**
     * Method to emit your event, with custom properties.
     * Property's key should be a valid ElasticSearch field name, and should not be started with "$" character.
     *
     * @param eventName   Name of the event you want to track
     * @param customProps Custom props as key-value (valid JSON object)
     */
    public void track(String eventName, JSONObject customProps) {
        Event event;
        JSONObject deviceInfo = deviceInfoManager.getDeviceInfo();
        String distinctId = identityManager.getDistinctId();
        event = new Event(eventName, distinctId, deviceInfo, customProps);
        taskManager.createEventTask(event);
    }

    /**
     * Method to map user's id in your system (given by sign-up action, etc.) with current
     * distinctId (auto-generated by GIAP for each user).
     * Should be called after user take sign-up action.
     *
     * @param userId Unique user's id in the product (provided by sign-up action)
     */
    public void alias(String userId) {
        taskManager.createAliasTask(userId);
    }

    /**
     * Method to map user's id in your system (given by log in action, etc.) with current
     * distinctId (auto-generated by GIAP for each user), to prevent missing events when user log in
     * on different devices.
     * Should be called after user take log in action.
     *
     * @param userId Unique user's id in the product (provided by log in action)
     */
    public void identify(String userId) {
        taskManager.createIdentifyTask(userId);
    }

    /**
     * Method to update custom properties of user's profile in GIAP system.
     * Example: email, economy_group, age, ...
     *
     * @param props Custom props as key-value (valid JSON object)
     */
    public void updateProfile(JSONObject props) {
        taskManager.createUpdateProfileTask(props);
    }

    /**
     * Increase/decrease numeric property
     * @param propertyName Property Name
     * @param value Increment/decrement value
     */
    public void increaseProperty(String propertyName, int value) {taskManager.createIncreasePropertyTask(propertyName, value);}

    /**
     * Increase/decrease numeric property
     * @param propertyName Property Name
     * @param value Increment/decrement value
     */
    public void increaseProperty(String propertyName, double value) {taskManager.createIncreasePropertyTask(propertyName, value);}

    /**
     * Append new elements to list property
     * @param propertyName Property Name
     * @param values New elements
     */
    public void appendToProperty(String propertyName, JSONArray values) {taskManager.createAppendToPropertyTask(propertyName, values);}


    /**
     * Remove elements from list property
     * @param propertyName Property Name
     * @param values Removed elements
     */
    public void removeFromProperty(String propertyName, JSONArray values) {taskManager.createRemoveFromPropertyTask(propertyName, values);}

    /**
     * Method to generate new distinctId for new user/visitor after current user take log out action.
     * Should always be called after user take log out action.
     */
    public void reset() {
        identityManager.generateNewDistinctId();
    }
}
