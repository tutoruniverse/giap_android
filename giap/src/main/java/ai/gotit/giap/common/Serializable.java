package ai.gotit.giap.common;

import org.json.JSONException;
import org.json.JSONObject;

public interface Serializable {
    JSONObject serialize() throws JSONException;
}
