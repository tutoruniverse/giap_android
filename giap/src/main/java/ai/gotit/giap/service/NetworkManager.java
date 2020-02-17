package ai.gotit.giap.service;

import org.json.JSONArray;
import org.json.JSONObject;

import ai.gotit.giap.util.Logger;

public class NetworkManager {
    public static boolean track(JSONArray bodyData) {
        //    TODO
        Logger.log("track(): " + bodyData.toString());
        return true;
    }

    public static boolean alias(JSONObject bodyData) {
        //    TODO
        Logger.log("alias(): " + bodyData.toString());
        return true;
    }

    public static boolean identify(String userId, String currentDistinctId) {
        //    TODO
        Logger.log("identity(): " + userId + " - " + currentDistinctId);
        return true;
    }

    public static void updateProfile() {
    }
}
