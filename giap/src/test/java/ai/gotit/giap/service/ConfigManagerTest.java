package ai.gotit.giap.service;

import android.app.Activity;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class ConfigManagerTest {
    private ConfigManager configManager;
    private final Activity context = mock(Activity.class);
    private final String serverUrl = "test_server_url";
    private final String token = "test_token";

    @Before
    public void beforeEach() throws Exception {
        configManager = spy(ConfigManager.makeInstance(context, serverUrl, token));
    }

    @Test
    public void makeInstance() {
        ConfigManager newInstance = ConfigManager.makeInstance(context, serverUrl, token);
        assertNotSame(newInstance, configManager);
        assertEquals(newInstance.getServerUrl(), serverUrl);
        assertEquals(newInstance.getToken(), token);
        assertSame(newInstance.getContext(), context);
    }

    @Test
    public void getContext() {
        assertSame(configManager.getContext(), context);
    }

    @Test
    public void setContext() {
        Activity newContext = mock(Activity.class);
        configManager.setContext(newContext);
        assertSame(configManager.getContext(), newContext);
    }

    @Test
    public void getServerUrl() {
        assertEquals(configManager.getServerUrl(), serverUrl);
    }

    @Test
    public void setServerUrl() {
        String newServerUrl = "new_server_url";
        configManager.setServerUrl(newServerUrl);
        assertEquals(configManager.getServerUrl(), newServerUrl);
    }

    @Test
    public void getToken() {
        assertEquals(configManager.getToken(), token);
    }

    @Test
    public void setToken() {
        String newToken = "new_token";
        configManager.setToken(newToken);
        assertEquals(configManager.getToken(), newToken);
    }
}