package ai.gotit.giap.entity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;

import ai.gotit.giap.common.Serializable;
import ai.gotit.giap.constant.CommonConstant;
import ai.gotit.giap.constant.EventProps;
import ai.gotit.giap.exception.GIAPInvalidPropsPrefixException;
import ai.gotit.giap.service.IdentityManager;
import ai.gotit.giap.util.Logger;

public class Event implements Serializable {
    private String name;
    private long time;
    private JSONObject customProps = new JSONObject();
    // TODO: device props

    public Event(String name) {
        this.name = name;
        updateTimestamp();
    }

    public Event(String name, JSONObject customProps) {
        this(name);
        Iterator<String> customPropsKeys = customProps.keys();
        while (customPropsKeys.hasNext()) {
            try {
                String key = customPropsKeys.next();
                Object value = customProps.get(key);
                addCustomProp(key, value);
            } catch (JSONException e) {
                Logger.error(e);
            }
        }
        this.customProps = customProps;
    }

    public void updateTimestamp() {
        long timeMilli = new Date().getTime();
        long timeSec = (long) Math.floor(timeMilli / 1000);
        time = timeSec;
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
        json.put(EventProps.DISTINCT_ID, IdentityManager.getInstance().getDistinctId());
        json.put(EventProps.NAME, name);
        json.put(EventProps.TIME, time);

        // Append customProps
        Iterator<String> customPropsKeys = customProps.keys();
        while (customPropsKeys.hasNext()) {
            String key = customPropsKeys.next();
            Object value = customProps.get(key);
            json.put(key, value);
        }

        return json;
    }
}
