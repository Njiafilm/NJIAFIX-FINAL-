package com.njiafix.app.admin;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;

public final class DeviceAdminHelper {
    private DeviceAdminHelper() {}

    public static ComponentName componentName(Activity a) {
        return new ComponentName(a, NjiaFixDeviceAdmin.class);
    }

    public static boolean isActive(Activity a) {
        DevicePolicyManager dpm = (DevicePolicyManager) a.getSystemService(Activity.DEVICE_POLICY_SERVICE);
        return dpm != null && dpm.isAdminActive(componentName(a));
    }

    /** Inafungua dirisha la Android la kuomba Device Admin KWENYE KIFAA HIKI. Jibu linarudi kwenye onActivityResult. */
    public static void requestActivation(Activity a, int requestCode) {
        Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName(a));
        i.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Inahitajika kwa 'Factory Reset (kifaa hiki)' - inafuta data YOTE ya kifaa hiki.");
        a.startActivityForResult(i, requestCode);
    }

    /** Kufuta data ya KIFAA HIKI HIKI (kinachoendesha NjiaFix). Halifanyi kazi kwa kifaa kingine kwa ADB. */
    public static void wipeThisDevice(Activity a) {
        DevicePolicyManager dpm = (DevicePolicyManager) a.getSystemService(Activity.DEVICE_POLICY_SERVICE);
        if (dpm != null) dpm.wipeData(0);
    }
}
