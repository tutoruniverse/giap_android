package ai.gotit.giap.entity;

import org.json.JSONException;
import org.json.JSONObject;

import ai.gotit.giap.common.Serializable;
import ai.gotit.giap.constant.TaskProps;

public class Task implements Serializable {
    private String type;
    private JSONObject data = null;

    public Task(String type) {
        this.type = type;
    }

    public Task(String type, JSONObject data) {
        this(type);
        this.data = data;
    }

    public Task(JSONObject serializedTask) throws JSONException {
        type = serializedTask.getString(TaskProps.TASK_TYPE);
        data = serializedTask.getJSONObject(TaskProps.DATA);
    }

    public String getType() {
        return type;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public JSONObject serialize() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(TaskProps.TASK_TYPE, type);
        json.put(TaskProps.DATA, data);
        return json;
    }
}
