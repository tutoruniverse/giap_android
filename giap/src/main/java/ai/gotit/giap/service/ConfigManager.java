package ai.gotit.giap.service;

import android.app.Activity;

public class ConfigManager {
    private Activity context = null;
    private String serverUrl = null;
    private String token = null;

    public ConfigManager() {
    }

    public ConfigManager(Activity context, String serverUrl, String token) {
        this.context = context;
        this.serverUrl = serverUrl;
        this.token = token;
    }

    public Activity getContext() {
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
