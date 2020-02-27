package ai.gotit.giap.service;

import org.junit.Before;
import org.junit.Test;

import ai.gotit.giap.constant.StorageKey;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class IdentityManagerTest {
    private IdentityManager identityManager;
    private Storage storage;
    private final String savedDistinctId = "123";


    @Before
    public void setUp() throws Exception {
        storage = mock(Storage.class);
        when(storage.getString(StorageKey.DISTINCT_ID)).thenReturn(savedDistinctId);
        identityManager = spy(IdentityManager.makeInstance(storage));
    }

    @Test
    public void constructor_DistinctId_NotExisted() {
        when(storage.getString(StorageKey.DISTINCT_ID)).thenReturn(null);
        identityManager = spy(IdentityManager.makeInstance(storage));
        assertNotEquals(savedDistinctId, identityManager.getDistinctId());
    }

    @Test
    public void constructor_DistinctId_Existed() {
        assertEquals(savedDistinctId, identityManager.getDistinctId());
    }

    @Test
    public void makeInstance() {
        IdentityManager newInstance = IdentityManager.makeInstance(storage);
        assertNotSame(newInstance, identityManager);
    }

    @Test
    public void getDistinctId() {
        String distinctId = identityManager.getDistinctId();
        assertEquals(distinctId, savedDistinctId);
    }

    @Test
    public void generateNewDistinctId() {
        String oldDistinctId = identityManager.getDistinctId();
        String distinctId = identityManager.generateNewDistinctId();
        verify(identityManager, times(1)).updateDistinctId(distinctId);
        assertNotEquals(distinctId, oldDistinctId);
    }

    @Test
    public void updateDistinctId() {
        final String distinctId = "456";
        identityManager.updateDistinctId(distinctId);
        verify(storage, times(1)).put(StorageKey.DISTINCT_ID, distinctId);
        assertEquals(identityManager.getDistinctId(), distinctId);
    }
}