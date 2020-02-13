package ai.gotit.giap.service;

import ai.gotit.giap.constant.RepositoryKey;
import ai.gotit.giap.exception.GIAPException;
import ai.gotit.giap.exception.GIAPInstanceExistsException;

public class IdentityManager {
    private static IdentityManager instance = null;
    private String distinctId = null;

    private IdentityManager() {
        distinctId = Repository.getInstance().getString(RepositoryKey.DISTINCT_ID);
    }

    public static IdentityManager initialize() throws GIAPException {
        if (instance != null) {
            throw new GIAPInstanceExistsException();
        }

        instance = new IdentityManager();
        return instance;
    }

    public static IdentityManager getInstance() {
        return instance;
    }
}
