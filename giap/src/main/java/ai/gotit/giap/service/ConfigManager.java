package ai.gotit.giap.service;

import android.app.Activity;
import android.content.Context;

public class ConfigManager {
    private Context context = null;
    private String serverUrl = null;
    private String token = null;

    public ConfigManager() {
    }

    public ConfigManager(Context context, String serverUrl, String token) {
        this.context = context;
        this.serverUrl = serverUrl;
        this.token = token;
    }

    public static ConfigManager makeInstance(Context context, String serverUrl, String token) {
        return new ConfigManager(context, serverUrl, token);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
