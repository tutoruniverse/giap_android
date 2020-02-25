package ai.gotit.giap;

import android.app.Activity;

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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        ExceptionHandler.class,
        Storage.class,
        DeviceInfoManager.class,
        NetworkManager.class,
        IdentityManager.class,
        TaskManager.class,
})
public class GIAPTest {
    private static final String serverUrl = "http://test.url";
    private static final String token = "123456";
    private static Activity context;
    private static GIAP giap;

    @Mock
    private static TaskManager taskManager;

    @Mock
    private static DeviceInfoManager deviceInfoManager;

    @Mock
    private static IdentityManager identityManager;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @BeforeClass
    public static void beforeAll() {
//        context = mock(Activity.class);
//
//        PowerMockito.spy(ExceptionHandler.class);
//        PowerMockito.spy(Storage.class);
//
//        PowerMockito.mockStatic(DeviceInfoManager.class);
//        when(DeviceInfoManager.getInstance()).thenReturn(deviceInfoManager);
//
//        PowerMockito.mockStatic(NetworkManager.class);
//
//        PowerMockito.mockStatic(IdentityManager.class);
//        when(IdentityManager.getInstance()).thenReturn(identityManager);
//
//        PowerMockito.mockStatic(TaskManager.class);
//        when(TaskManager.getInstance()).thenReturn(taskManager);
//
//        giap = GIAP.initialize(serverUrl, token, context);
    }

    @Before
    public void beforeEach() {
        context = mock(Activity.class);

        PowerMockito.spy(ExceptionHandler.class);
        PowerMockito.spy(Storage.class);

        PowerMockito.mockStatic(DeviceInfoManager.class);
        when(DeviceInfoManager.getInstance()).thenReturn(deviceInfoManager);

        PowerMockito.mockStatic(NetworkManager.class);

        PowerMockito.mockStatic(IdentityManager.class);
        when(IdentityManager.getInstance()).thenReturn(identityManager);

        PowerMockito.mockStatic(TaskManager.class);
        when(TaskManager.getInstance()).thenReturn(taskManager);

        if (GIAP.getInstance() == null) {
            GIAP.initialize(serverUrl, token, context);
        }
        giap = GIAP.getInstance();
    }

    @After
    public void afterEach() throws Exception {
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
        ExceptionHandler.initialize();
        PowerMockito.verifyStatic(Storage.class, times(1));
        Storage.initialize();
        PowerMockito.verifyStatic(DeviceInfoManager.class, times(1));
        DeviceInfoManager.initialize();
        PowerMockito.verifyStatic(NetworkManager.class, times(1));
        NetworkManager.initialize();
        PowerMockito.verifyStatic(IdentityManager.class, times(1));
        IdentityManager.initialize();
        PowerMockito.verifyStatic(TaskManager.class, times(1));
        TaskManager.initialize();

        // Should set configs
        ConfigManager configManager = ConfigManager.getInstance();
        assertEquals(configManager.getServerUrl(), serverUrl);
        assertEquals(configManager.getToken(), token);
        assertEquals(configManager.getContext(), context);
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
    public void reset() {
        giap.reset();
        verify(identityManager, times(1)).generateNewDistinctId();
    }
}