package ai.gotit.giap.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ai.gotit.giap.BuildConfig;
import ai.gotit.giap.constant.DeviceInfoProps;
import ai.gotit.giap.constant.StorageKey;
import ai.gotit.giap.util.Logger;

public class DeviceInfoManager {
    private Map<String, Object> staticProps = new HashMap<>();
    private ConfigManager configManager;
    private Storage storage;

    public DeviceInfoManager(ConfigManager configManager, Storage storage) {
        this.configManager = configManager;
        this.storage = storage;

        Activity context = configManager.getContext();
        PackageManager packageManager = context.getPackageManager();

        // Get version's info
        Long foundAppVersionCode = null;
        String foundAppVersionName = null;
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            foundAppVersionCode = (long) packageInfo.versionCode;
            foundAppVersionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.warn("System information constructed with a context that apparently doesn't exist.");
        }

        // Get NFC / Telephony availability
        Boolean foundNFC = null;
        Boolean foundTelephony = null;
        // We can't count on these features being available, since we need to
        // run on old devices. Thus, the reflection fandango below...
        Class<? extends PackageManager> packageManagerClass = packageManager.getClass();
        Method hasSystemFeatureMethod = null;
        try {
            hasSystemFeatureMethod = packageManagerClass.getMethod("hasSystemFeature", String.class);
        } catch (NoSuchMethodException e) {
            // Nothing, this is an expected outcome
        }
        if (null != hasSystemFeatureMethod) {
            try {
                foundNFC = (Boolean) hasSystemFeatureMethod.invoke(packageManager, "android.hardware.nfc");
                foundTelephony = (Boolean) hasSystemFeatureMethod.invoke(packageManager, "android.hardware.telephony");
            } catch (InvocationTargetException | IllegalAccessException e) {
                Logger.warn("System version appeared to support PackageManager.hasSystemFeature, but we were unable to call it.");
            }
        }

        staticProps.put(DeviceInfoProps.DEVICE_ID, getDeviceId());
        staticProps.put(DeviceInfoProps.OS, "android");
        staticProps.put(DeviceInfoProps.LIB, "GIAP-android");
        staticProps.put(DeviceInfoProps.LIB_VERSION, BuildConfig.VERSION_NAME);
        DisplayMetrics displayMetrics = getScreenMetrics();
        staticProps.put(DeviceInfoProps.SCREEN_HEIGHT, displayMetrics.heightPixels);
        staticProps.put(DeviceInfoProps.SCREEN_WIDTH, displayMetrics.widthPixels);
        staticProps.put(DeviceInfoProps.SCREEN_DPI, displayMetrics.densityDpi);
        staticProps.put(DeviceInfoProps.OS_VERSION, Build.VERSION.RELEASE);
        staticProps.put(DeviceInfoProps.APP_BUILD_NUMBER, foundAppVersionCode);
        staticProps.put(DeviceInfoProps.APP_VERSION_STRING, foundAppVersionName);
        staticProps.put(DeviceInfoProps.CARRIER, getCarrier());
        staticProps.put(DeviceInfoProps.MANUFACTURER, Build.MANUFACTURER);
        staticProps.put(DeviceInfoProps.MODEL, Build.MODEL);
        staticProps.put(DeviceInfoProps.BRAND, Build.BRAND);
        staticProps.put(DeviceInfoProps.BLUETOOTH_VERSION, getBluetoothVersion());
        staticProps.put(DeviceInfoProps.HAS_NFC, foundNFC);
        staticProps.put(DeviceInfoProps.HAS_TELEPHONE, foundTelephony);
        staticProps.put(DeviceInfoProps.GOOGLE_PLAY_SERVICES, getGooglePlayServices());
    }

    public JSONObject getDeviceInfo() {
        // Copy all static props
        JSONObject deviceInfo = new JSONObject(staticProps);
        try {
            deviceInfo.put(DeviceInfoProps.WIFI, isWifiConnected());
            deviceInfo.put(DeviceInfoProps.BLUETOOTH_ENABLED, isBluetoothEnabled());
        } catch (JSONException e) {
            Logger.error(e);
        }
        return deviceInfo;
    }

    private String getDeviceId() {
        String deviceId = storage.getString(StorageKey.DEVICE_ID);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            storage.put(StorageKey.DEVICE_ID, deviceId);
        }
        return deviceId;
    }

    private DisplayMetrics getScreenMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        configManager
                .getContext()
                .getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        return displayMetrics;
    }

    private String getCarrier() {
        String carrier = null;

        TelephonyManager telephonyManager = (TelephonyManager) configManager
                .getContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (null != telephonyManager) {
            carrier = telephonyManager.getNetworkOperatorName();
        }

        return carrier;
    }

    private String getBluetoothVersion() {
        String bluetoothVersion = null;
        PackageManager packageManager = configManager
                .getContext()
                .getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            bluetoothVersion = "ble";
        } else if (packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            bluetoothVersion = "classic";
        }
        return bluetoothVersion;
    }

    private String getGooglePlayServices() {
        String googlePlayServices = null;
        try {
            try {
                final int servicesAvailable = GoogleApiAvailability
                        .getInstance()
                        .isGooglePlayServicesAvailable(configManager.getContext());
                switch (servicesAvailable) {
                    case ConnectionResult.SUCCESS:
                        googlePlayServices = "available";
                        break;
                    case ConnectionResult.SERVICE_MISSING:
                        googlePlayServices = "missing";
                        break;
                    case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                        googlePlayServices = "out of date";
                        break;
                    case ConnectionResult.SERVICE_DISABLED:
                        googlePlayServices = "disabled";
                        break;
                    case ConnectionResult.SERVICE_INVALID:
                        googlePlayServices = "invalid";
                        break;
                }
            } catch (RuntimeException e) {
                googlePlayServices = "not configured";
            }
        } catch (NoClassDefFoundError e) {
            googlePlayServices = "not included";
        }
        return googlePlayServices;
    }

    @SuppressLint("MissingPermission")
    @SuppressWarnings("MissingPermission")
    private Boolean isWifiConnected() {
        Activity context = configManager.getContext();
        Boolean wifiConnected = null;
        if (PackageManager.PERMISSION_GRANTED == context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            wifiConnected = (
                    networkInfo != null
                            && networkInfo.getType() == ConnectivityManager.TYPE_WIFI
                            && networkInfo.isConnected()
            );
        }
        return wifiConnected;
    }

    @SuppressLint("MissingPermission")
    @SuppressWarnings("MissingPermission")
    private Boolean isBluetoothEnabled() {
        Activity context = configManager.getContext();
        Boolean isBluetoothEnabled = null;
        try {
            PackageManager pm = context.getPackageManager();
            int hasBluetoothPermission = pm.checkPermission(Manifest.permission.BLUETOOTH, context.getPackageName());
            if (hasBluetoothPermission == PackageManager.PERMISSION_GRANTED) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null) {
                    isBluetoothEnabled = bluetoothAdapter.isEnabled();
                }
            }
        } catch (SecurityException e) {
            // do nothing since we don't have permissions
        } catch (NoClassDefFoundError e) {
            // Some phones doesn't have this class. Just ignore it
        }
        return isBluetoothEnabled;
    }
}
