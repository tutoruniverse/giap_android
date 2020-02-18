package ai.gotit.giap.service;

import java.util.UUID;

import ai.gotit.giap.constant.RepositoryKey;
import ai.gotit.giap.exception.GIAPInstanceExistsException;

public class IdentityManager {
    private static IdentityManager instance = null;
    private String distinctId = null;

    private IdentityManager() {
        String distinctId = Repository.getInstance().getString(RepositoryKey.DISTINCT_ID);
        if (distinctId == null) {
            generateNewDistinctId();
        } else {
            this.distinctId = distinctId;
        }
    }

    public static IdentityManager initialize() {
        if (instance != null) {
            throw new GIAPInstanceExistsException();
        }

        instance = new IdentityManager();
        return instance;
    }

    public static IdentityManager getInstance() {
        return instance;
    }

    public String getDistinctId() {
        return distinctId;
    }

    public void generateNewDistinctId() {
        distinctId = UUID.randomUUID().toString();
        updateDistinctId(distinctId);
    }

    public void updateDistinctId(String distinctId) {
        this.distinctId = distinctId;
        Repository.getInstance().put(RepositoryKey.DISTINCT_ID, distinctId);
    }
}
