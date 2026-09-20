package com.njiafix.app.admin;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/** Huruhusu NjiaFix kuwa Device Admin KWENYE KIFAA HIKI HIKI (siyo kifaa kingine kupitia ADB). */
public class NjiaFixDeviceAdmin extends DeviceAdminReceiver {
    @Override
    public void onEnabled(Context context, Intent intent) {
        Toast.makeText(context, "NjiaFix: Device Admin imewezeshwa", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Toast.makeText(context, "NjiaFix: Device Admin imezimwa", Toast.LENGTH_SHORT).show();
    }
}
