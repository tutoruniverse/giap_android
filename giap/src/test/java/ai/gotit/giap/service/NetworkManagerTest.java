package ai.gotit.giap.service;

import android.app.Activity;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

// TODO: can not mock Volley
public class NetworkManagerTest {
    private NetworkManager networkManager;
    private ConfigManager configManager;

    @Before
    public void setUp() throws Exception {
        configManager = new ConfigManager();
        configManager.setServerUrl("123");
        configManager.setToken("456");
        configManager.setContext(mock(Activity.class));

        networkManager = spy(NetworkManager.makeInstance(configManager));
    }

    @Test
    public void track() {
    }

    @Test
    public void alias() {
    }

    @Test
    public void identify() {
    }

    @Test
    public void updateProfile() {
    }
}