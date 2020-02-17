package ai.gotit.giap.service;

import android.app.Activity;

import ai.gotit.giap.constant.DefaultConfig;

public class ConfigManager {
    private static ConfigManager instance = null;
    private Activity context = null;
    private String serverUrl = null;
    private String token = null;
    private long tasksFlushingInterval = DefaultConfig.TASKS_FLUSHING_INTERVAL;
    private int requestRetryTimes = DefaultConfig.REQUEST_RETRY_TIMES;

    private ConfigManager() {
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
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

    public long getTasksFlushingInterval() {
        return tasksFlushingInterval;
    }

    public void setTasksFlushingInterval(long tasksFlushingInterval) {
        this.tasksFlushingInterval = tasksFlushingInterval;
    }

    public int getRequestRetryTimes() {
        return requestRetryTimes;
    }

    public void setRequestRetryTimes(int requestRetryTimes) {
        this.requestRetryTimes = requestRetryTimes;
    }
}
