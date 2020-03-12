package ai.gotit.giap.entity;

import androidx.annotation.VisibleForTesting;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;

import ai.gotit.giap.common.Serializable;
import ai.gotit.giap.constant.CommonConstant;
import ai.gotit.giap.constant.EventProps;
import ai.gotit.giap.exception.GIAPInvalidPropsPrefixException;
import ai.gotit.giap.util.Logger;

public class Event implements Serializable {
    private String name;
    private String distinctId;
    private Long time;
    private JSONObject customProps = new JSONObject();
    private JSONObject deviceInfo;

    public Event(String name, String distinctId, JSONObject deviceInfo, JSONObject customProps) {
        updateTimestamp();
        this.name = name;
        this.distinctId = distinctId;
        this.deviceInfo = deviceInfo;
        if (customProps != null) {
            try {
                Iterator<String> customPropsKeys = customProps.keys();
                while (customPropsKeys.hasNext()) {
                    String key = customPropsKeys.next();
                    Object value = null;
                    if (!customProps.isNull(key)) {
                        value = customProps.get(key);
                    }
                    addCustomProp(key, value);
                }
                this.customProps = customProps;
            } catch (JSONException e) {
                Logger.error(e);
            }
        }
    }

    public void updateTimestamp() {
        time = new Date().getTime();
    }

    public void addCustomProp(String key, Object value) {
        if (key.startsWith(CommonConstant.DEFAULT_PROP_PREFIX)) {
            throw new GIAPInvalidPropsPrefixException();
        }
        try {
            customProps.put(key, value);
        } catch (JSONException exception) {
            Logger.error(exception);
        }
    }

    public JSONObject serialize() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(EventProps.DISTINCT_ID, distinctId);
        json.put(EventProps.NAME, name);
        json.put(EventProps.TIME, time);

        // Append device's info
        if (deviceInfo != null) {
            Iterator<String> deviceInfoKeys = deviceInfo.keys();
            while (deviceInfoKeys.hasNext()) {
                String key = deviceInfoKeys.next();
                Object value = null;
                if (!deviceInfo.isNull(key)) {
                    value = deviceInfo.get(key);
                }
                json.put(key, value);
            }
        }

        // Append customProps
        if (customProps != null) {
            Iterator<String> customPropsKeys = customProps.keys();
            while (customPropsKeys.hasNext()) {
                String key = customPropsKeys.next();
                Object value = null;
                if (!customProps.isNull(key)) {
                    value = customProps.get(key);
                }
                json.put(key, value);
            }
        }

        return json;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    long getTime() {
        return time;
    }
}
