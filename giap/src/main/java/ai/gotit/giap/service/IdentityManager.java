package ai.gotit.giap.service;

import java.util.UUID;

import ai.gotit.giap.constant.StorageKey;
import ai.gotit.giap.exception.GIAPInstanceExistsException;

public class IdentityManager {
    private static IdentityManager instance = null;
    private String distinctId = null;

    private IdentityManager() {
        String distinctId = Storage.getInstance().getString(StorageKey.DISTINCT_ID);
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
        Storage.getInstance().put(StorageKey.DISTINCT_ID, distinctId);
    }
}
