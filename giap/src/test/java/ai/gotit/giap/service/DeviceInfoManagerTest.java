package ai.gotit.giap.service;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Map;

import ai.gotit.giap.constant.DeviceInfoProps;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceInfoManagerTest {
    private DeviceInfoManager deviceInfoManager;
    private ConfigManager configManager;
    private Storage storage;

    @Before
    public void setUp() throws Exception {
        Activity context = mock(Activity.class);

        // Mock package info
        PackageManager packageManager = mock(PackageManager.class);
        PackageInfo packageInfo = mock(PackageInfo.class);
        packageInfo.versionCode = 1;
        packageInfo.versionName = "1.0";
        when(packageManager.getPackageInfo(anyString(), anyInt())).thenReturn(packageInfo);
        when(context.getPackageManager()).thenReturn(packageManager);
        when(context.getPackageName()).thenReturn("giap");

        // Mock display info
        WindowManager windowManager = mock(WindowManager.class);
        Display display = mock(Display.class);
        when(context.getWindowManager()).thenReturn(windowManager);
        when(windowManager.getDefaultDisplay()).thenReturn(display);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                DisplayMetrics displayMetrics = ((DisplayMetrics) args[0]);
                displayMetrics.heightPixels = 200;
                displayMetrics.widthPixels = 100;
                displayMetrics.densityDpi = 2;
                return null;
            }
        }).when(display).getMetrics(any(DisplayMetrics.class));

        // Mock WIFI
        ConnectivityManager connManager = mock(ConnectivityManager.class);
        NetworkInfo networkInfo = mock(NetworkInfo.class);
        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connManager);
        when(connManager.getActiveNetworkInfo()).thenReturn(networkInfo);
        when(networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_WIFI);
        when(networkInfo.isConnected()).thenReturn(true);
        // Mock telephone info
        TelephonyManager telephonyManager = mock(TelephonyManager.class);
        when(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(telephonyManager);
        when(telephonyManager.getNetworkOperatorName()).thenReturn("carrier name");

        configManager = ConfigManager.makeInstance(context, "123", "456");
        storage = mock(Storage.class);

        deviceInfoManager = spy(DeviceInfoManager.makeInstance(configManager, storage));
    }

    @Test
    public void makeInstance() {
        DeviceInfoManager newInstance = DeviceInfoManager.makeInstance(configManager, storage);
        assertNotSame(newInstance, deviceInfoManager);
    }

    @Test
    public void getDeviceInfo() {
        deviceInfoManager.getDeviceInfo();
        verify(deviceInfoManager, times(1)).getDeviceInfoAsMap();
    }

    @Test
    public void staticProps() {
        Map<String, Object> staticProps = deviceInfoManager.getStaticProps();
        assertTrue(staticProps.containsKey(DeviceInfoProps.DEVICE_ID));
        assertTrue(staticProps.containsKey(DeviceInfoProps.OS));
        assertTrue(staticProps.containsKey(DeviceInfoProps.LIB));
        assertTrue(staticProps.containsKey(DeviceInfoProps.LIB_VERSION));
        assertTrue(staticProps.containsKey(DeviceInfoProps.SCREEN_HEIGHT));
        assertTrue(staticProps.containsKey(DeviceInfoProps.SCREEN_DPI));
        assertTrue(staticProps.containsKey(DeviceInfoProps.OS_VERSION));
        assertTrue(staticProps.containsKey(DeviceInfoProps.APP_BUILD_NUMBER));
        assertTrue(staticProps.containsKey(DeviceInfoProps.APP_VERSION_STRING));
        assertTrue(staticProps.containsKey(DeviceInfoProps.CARRIER));
        assertTrue(staticProps.containsKey(DeviceInfoProps.MANUFACTURER));
        assertTrue(staticProps.containsKey(DeviceInfoProps.MODEL));
        assertTrue(staticProps.containsKey(DeviceInfoProps.BRAND));
        assertTrue(staticProps.containsKey(DeviceInfoProps.BLUETOOTH_VERSION));
        assertTrue(staticProps.containsKey(DeviceInfoProps.HAS_NFC));
        assertTrue(staticProps.containsKey(DeviceInfoProps.HAS_TELEPHONE));
        assertTrue(staticProps.containsKey(DeviceInfoProps.GOOGLE_PLAY_SERVICES));
    }
}