package ai.gotit.giap.service;

import android.app.Activity;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ai.gotit.giap.constant.CommonProps;
import ai.gotit.giap.exception.GIAPInstanceExistsException;
import ai.gotit.giap.util.Logger;
import androidx.annotation.Nullable;

public class NetworkManager {
    private static NetworkManager instance = null;
    private RequestQueue requestQueue;

    private NetworkManager(Activity context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public static NetworkManager initialize() {
        if (instance != null) {
            throw new GIAPInstanceExistsException();
        }

        Activity context = ConfigManager.getInstance().getContext();
        instance = new NetworkManager(context);
        return instance;
    }

    public static NetworkManager getInstance() {
        return instance;
    }

    private void request(final int method, String endpoint, Map<String, String> params, @Nullable JSONObject body, Listener<JSONObject> callback, ErrorListener errorCallback) {
        Uri.Builder builder = new Uri.Builder();
        String serverUrl = ConfigManager.getInstance().getServerUrl();
        if (!serverUrl.startsWith("http")) {
            builder.scheme("https");
            builder.encodedAuthority(serverUrl);
        } else {
            builder.encodedPath(serverUrl);
        }
        builder.appendEncodedPath(endpoint);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        String url = builder.build().toString();
        JsonObjectRequest requestJson = new JsonObjectRequest(method, url, body, callback, errorCallback) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + ConfigManager.getInstance().getToken());
                if (method != Method.GET) {
                    params.put("Content-Type", "application/json");
                }
                return params;
            }
        };
        requestQueue.add(requestJson);
        Logger.log(
                "REQUEST: sent a request"
                        + " - " + requestJson.getMethod()
                        + " " + requestJson.getUrl()
                        + (body != null ? " - " + body.toString() : "")
        );
    }

    public void track(JSONArray eventList, Listener<JSONObject> callback, ErrorListener errorCallback) {
        JSONObject bodyData = new JSONObject();
        try {
            bodyData.put("events", eventList);
        } catch (JSONException e) {
            Logger.error(e);
        }
        request(Request.Method.POST, "events", null, bodyData, callback, errorCallback);
    }

    public void alias(JSONObject bodyData, Listener<JSONObject> callback, ErrorListener errorCallback) {
        request(Request.Method.POST, "alias", null, bodyData, callback, errorCallback);
    }

    public void identify(String userId, String currentDistinctId, Listener<JSONObject> callback, ErrorListener errorCallback) {
        String endpoint = "alias/" + userId;
        Map<String, String> params = new HashMap<>();
        params.put(CommonProps.CURRENT_DISTINCT_ID, currentDistinctId);
        request(Request.Method.GET, endpoint, params, null, callback, errorCallback);
    }

    public void updateProfile(JSONObject data, Listener<JSONObject> callback, ErrorListener errorCallback) {
        String distinctId = null;
        JSONObject bodyData = null;
        try {
            distinctId = data.getString(CommonProps.CURRENT_DISTINCT_ID);
            bodyData = new JSONObject(data.toString());
            bodyData.remove(CommonProps.CURRENT_DISTINCT_ID);
        } catch (JSONException e) {
            Logger.error(e);
        }
        String endpoint = "profiles/" + distinctId;
        request(Request.Method.PUT, endpoint, null, bodyData, callback, errorCallback);
    }
}
