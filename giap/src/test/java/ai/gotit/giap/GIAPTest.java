package ai.gotit.giap;

import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Collections;

import ai.gotit.giap.entity.Event;
import ai.gotit.giap.exception.GIAPInstanceExistsException;
import ai.gotit.giap.mock.JSONObjectMock;
import ai.gotit.giap.service.ConfigManager;
import ai.gotit.giap.service.DeviceInfoManager;
import ai.gotit.giap.service.ExceptionHandler;
import ai.gotit.giap.service.IdentityManager;
import ai.gotit.giap.service.NetworkManager;
import ai.gotit.giap.service.Storage;
import ai.gotit.giap.service.TaskManager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        ExceptionHandler.class,
        ConfigManager.class,
        Storage.class,
        DeviceInfoManager.class,
        NetworkManager.class,
        IdentityManager.class,
        TaskManager.class,
})
public class GIAPTest {
    private final String serverUrl = "http://test.url";
    private final String token = "123456";
    private Activity context;
    private GIAP giap;

    @Mock
    private ExceptionHandler exceptionHandler;
    @Mock
    private ConfigManager configManager;
    @Mock
    private Storage storage;
    @Mock
    private DeviceInfoManager deviceInfoManager;
    @Mock
    private NetworkManager networkManager;
    @Mock
    private IdentityManager identityManager;
    @Mock
    private TaskManager taskManager;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @BeforeClass
    public static void beforeAll() {
    }

    @Before
    public void beforeEach() throws Exception {
        context = mock(Activity.class);

        PowerMockito.mockStatic(ExceptionHandler.class);
        when(ExceptionHandler.makeInstance(any(GIAP.class)))
                .thenReturn(exceptionHandler);

        PowerMockito.mockStatic(ConfigManager.class);
        when(ConfigManager.makeInstance(any(Activity.class), anyString(), anyString()))
                .thenReturn(configManager);

        PowerMockito.mockStatic(Storage.class);
        when(Storage.makeInstance(any(ConfigManager.class)))
                .thenReturn(storage);

        PowerMockito.mockStatic(DeviceInfoManager.class);
        when(DeviceInfoManager.makeInstance(any(ConfigManager.class), any(Storage.class)))
                .thenReturn(deviceInfoManager);

        PowerMockito.mockStatic(NetworkManager.class);
        when(NetworkManager.makeInstance(any(ConfigManager.class)))
                .thenReturn(networkManager);

        PowerMockito.mockStatic(IdentityManager.class);
        when(IdentityManager.makeInstance(any(Storage.class)))
                .thenReturn(identityManager);

        PowerMockito.mockStatic(TaskManager.class);
        when(TaskManager.makeInstance(any(Storage.class), any(IdentityManager.class), any(NetworkManager.class)))
                .thenReturn(taskManager);

        giap = GIAP.initialize(serverUrl, serverUrl, context);
    }

    @After
    public void afterEach() throws Exception {
        TestHelper.resetSingleton(GIAP.class);
    }

    @Test
    public void initialize() {
        // Initialized -> initialize() again -> should throw error
        boolean thrownException = false;
        try {
            GIAP.initialize(serverUrl, token, context);
        } catch (GIAPInstanceExistsException e) {
            thrownException = true;
        }
        assertTrue(thrownException);

        // Should initialized all services
        PowerMockito.verifyStatic(ExceptionHandler.class, times(1));
        ExceptionHandler.makeInstance(any(GIAP.class));

        PowerMockito.verifyStatic(ConfigManager.class, times(1));
        ConfigManager.makeInstance(any(Activity.class), anyString(), anyString());

        PowerMockito.verifyStatic(Storage.class, times(1));
        Storage.makeInstance(any(ConfigManager.class));

        PowerMockito.verifyStatic(DeviceInfoManager.class, times(1));
        DeviceInfoManager.makeInstance(any(ConfigManager.class), any(Storage.class));

        PowerMockito.verifyStatic(NetworkManager.class, times(1));
        NetworkManager.makeInstance(any(ConfigManager.class));

        PowerMockito.verifyStatic(IdentityManager.class, times(1));
        IdentityManager.makeInstance(any(Storage.class));

        PowerMockito.verifyStatic(TaskManager.class, times(1));
        TaskManager.makeInstance(any(Storage.class), any(IdentityManager.class), any(NetworkManager.class));
    }

    @Test
    public void getInstance() {
        GIAP instance = GIAP.getInstance();
        assertNotNull(instance);
    }

    @Test
    public void track() {
        giap.track("event_name");
        verify(taskManager, times(1)).createEventTask(any(Event.class));

        JSONObject mockJSONObject = new JSONObjectMock().getMock();
        giap.track("event_name", mockJSONObject);
        verify(taskManager, times(2)).createEventTask(any(Event.class));
    }

    @Test
    public void alias() {
        String userId = "test_user_id";
        giap.alias(userId);
        verify(taskManager, times(1)).createAliasTask(userId);
    }

    @Test
    public void identify() {
        String userId = "test_user_id";
        giap.identify(userId);
        verify(taskManager, times(1)).createIdentifyTask(userId);
    }

    @Test
    public void updateProfile() {
        JSONObject mockJSONObject = new JSONObjectMock().getMock();
        giap.updateProfile(mockJSONObject);
        verify(taskManager, times(1)).createUpdateProfileTask(mockJSONObject);
    }

    @Test
    public void increaseIntProperty() {
        giap.increaseProperty("count", 1);
        verify(taskManager, times(1)).createIncreasePropertyTask("count", 1);
    }

    @Test
    public void increaseDoubleProperty() {
        giap.increaseProperty("count", 0.1);
        verify(taskManager, times(1)).createIncreasePropertyTask("count", 0.1);
    }

    @Test
    public void appendToProperty() {
        JSONArray tags = new JSONArray();
        tags.put("red");
        tags.put("blue");
        giap.appendToProperty("tags", tags);
        verify(taskManager, times(1)).createAppendToPropertyTask("tags", tags);
    }

    @Test
    public void removeFromProperty() {
        JSONArray tags = new JSONArray();
        tags.put("red");
        tags.put("blue");
        giap.removeFromProperty("tags", tags);
        verify(taskManager, times(1)).createRemoveFromPropertyTask("tags", tags);
    }

    @Test
    public void reset() {
        giap.reset();
        verify(identityManager, times(1)).generateNewDistinctId();
    }

    @Test
    public void onUncaughtException() {
        giap.onUncaughtException();
        verify(taskManager, times(1)).stop();
    }

    @Test
    public void onPause() {
        giap.onPause();
        verify(taskManager, times(1)).stop();
    }

    @Test
    public void onResume() {
        giap.onResume();
        verify(taskManager, times(1)).restart();
    }
}