package ai.gotit.giap.service;

import android.app.Activity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import ai.gotit.giap.mock.MockHttpStack;

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

        BaseHttpStack stack = new MockHttpStack();

        networkManager = spy(new NetworkManager(configManager, stack));
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