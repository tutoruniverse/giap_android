package ai.gotit.giap.service;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import ai.gotit.giap.constant.CommonConstant;
import ai.gotit.giap.exception.GIAPInstanceExistsException;

public class Repository {
    private static Repository instance = null;
    private SharedPreferences pref;

    private Repository(Activity context) {
        // We use token in storage's name to prevent different projects using others's data
        String token = ConfigManager.getInstance().getToken();
        String sharedPreferencesName = CommonConstant.SHARED_PREFERENCES_PREFIX + token;
        pref = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
    }

    public static Repository initialize() {
        if (instance != null) {
            throw new GIAPInstanceExistsException();
        }

        Activity context = ConfigManager.getInstance().getContext();
        instance = new Repository(context);
        return instance;
    }

    public static Repository getInstance() {
        return instance;
    }

    public void put(String key, String value) {
        Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void put(String key, Boolean value) {
        Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void put(String key, Integer value) {
        Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public void put(String key, Float value) {
        Editor editor = pref.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public String getString(String key) {
        if (!pref.contains(key)) return null;
        return pref.getString(key, null);
    }

    public Boolean getBoolean(String key) {
        if (!pref.contains(key)) return null;
        return pref.getBoolean(key, false);
    }

    public Integer getInt(String key) {
        if (!pref.contains(key)) return null;
        return pref.getInt(key, 0);
    }

    public Float getFloat(String key) {
        if (!pref.contains(key)) return null;
        return pref.getFloat(key, 0);
    }

    public void remove(String key) {
        Editor editor = pref.edit();
        editor.remove(key);
        editor.commit();
    }
}
