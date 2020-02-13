package ai.gotit.giap;

import android.app.Activity;

import ai.gotit.giap.exception.GIAPException;
import ai.gotit.giap.exception.GIAPInstanceExistsException;
import ai.gotit.giap.service.Configuration;
import ai.gotit.giap.service.Repository;

public class GIAP {

    private static GIAP instance = null;

    public static GIAP getInstance() {
        return instance;
    }

    private GIAP() {
    }

    public static GIAP initialize(String serverUrl, String token, Activity activity) throws GIAPException {
        if (instance != null) {
            throw new GIAPInstanceExistsException();
        }

        Configuration configuration = Configuration.getInstance();
        configuration.setServerUrl(serverUrl);
        configuration.setToken(token);

        Repository.initialize(activity);

        instance = new GIAP();
        return instance;
    }

}
