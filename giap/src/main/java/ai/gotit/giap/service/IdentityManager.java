package ai.gotit.giap.service;

import java.util.UUID;

import ai.gotit.giap.constant.StorageKey;

public class IdentityManager {
    private String distinctId = null;
    private Storage storage;

    public IdentityManager(Storage storage) {
        this.storage = storage;

        String distinctId = storage.getString(StorageKey.DISTINCT_ID);
        if (distinctId == null) {
            generateNewDistinctId();
        } else {
            this.distinctId = distinctId;
        }
    }

    public static IdentityManager makeInstance(Storage storage) {
        return new IdentityManager(storage);
    }

    public String getDistinctId() {
        return distinctId;
    }

    public String generateNewDistinctId() {
        distinctId = UUID.randomUUID().toString();
        updateDistinctId(distinctId);
        return distinctId;
    }

    public void updateDistinctId(String distinctId) {
        this.distinctId = distinctId;
        storage.put(StorageKey.DISTINCT_ID, distinctId);
    }
}
