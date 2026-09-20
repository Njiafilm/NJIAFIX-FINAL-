package com.njiafix.app.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.njiafix.app.R;
import com.njiafix.app.admin.DeviceAdminHelper;
import com.njiafix.app.api.ApiClient;

public class UnlockActivity extends AppCompatActivity {
    private static final int REQ_DEVICE_ADMIN = 501;
    private TextView guide;
    private ApiClient api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);
        setTitle("Unlock / Passkey");
        guide = findViewById(R.id.tvGuide);
        api = new ApiClient(this);

        findViewById(R.id.btnGoogle).setOnClickListener(v -> openUrl(
                "https://www.google.com/android/find",
                "🌐 Google Find My Device imefunguliwa. Ingia account ya mteja huko."));
        findViewById(R.id.btnSamsung).setOnClickListener(v -> openUrl(
                "https://findmymobile.samsung.com",
                "📱 Samsung Find My Mobile imefunguliwa. Ingia account ya mteja huko."));

        findViewById(R.id.btnDeviceAdmin).setOnClickListener(v -> {
            if (DeviceAdminHelper.isActive(this)) {
                set("✅ Device Admin TAYARI imewezeshwa kwenye kifaa hiki.");
            } else {
                set("Inafungua dirisha la Android la kuwezesha Device Admin...");
                DeviceAdminHelper.requestActivation(this, REQ_DEVICE_ADMIN);
            }
        });

        findViewById(R.id.btnFactoryLocal).setOnClickListener(v -> {
            if (!DeviceAdminHelper.isActive(this)) {
                set("❌ Device Admin haijawezeshwa KWENYE KIFAA HIKI. Bonyeza 'Wezesha Device Admin' kwanza.");
                return;
            }
            confirm("Factory Reset - kifaa hiki",
                    "Hii inafuta DATA YOTE ya simu hii hii inayoendesha NjiaFix (siyo simu nyingine "
                            + "iliyounganishwa kwa USB). Haiwezi kutenduliwa.\n\nEndelea?",
                    () -> { set("Inafuta data..."); DeviceAdminHelper.wipeThisDevice(this); });
        });

        findViewById(R.id.btnFactory).setOnClickListener(v -> confirm(
                "Factory Reset - kifaa kingine (ADB)",
                "Itajaribu 'am broadcast MASTER_CLEAR' kwenye kifaa kilichounganishwa kwa USB. "
                        + "UKWELI: Android inazuia amri hii isipokuwa kwa app ya Device Owner "
                        + "iliyowekwa TAYARI kwenye kifaa hicho (hatuwezi kuiwezesha kwa mbali kwa ADB "
                        + "peke yake) - kwa vifaa vingi itakataliwa. Kifaa lazima kiwe kimefunguliwa "
                        + "na USB Debugging ON.\n\nEndelea?",
                () -> run("factory_reset")));

        findViewById(R.id.btnPkSettings).setOnClickListener(v -> set(
                "⚙️ Passkey – Settings (OFFLINE)\n\n" +
                "Simu IMEFUNGULIWA:\n" +
                "Settings → Passwords / Passkeys → Delete\n\n" +
                "Hatua hii inafanywa mkononi kwenye kifaa - haiwezi kufanywa kwa mbali."));
        findViewById(R.id.btnPkGoogle).setOnClickListener(v -> openUrl(
                "https://myaccount.google.com/security",
                "🌐 Ukurasa wa usalama wa Google umefunguliwa. Nenda Passkeys → Delete."));
        findViewById(R.id.btnPkAdb).setOnClickListener(v -> confirm(
                "Ondoa Passkey (ADB)",
                "Itajaribu kuondoa passkey/lock kupitia ADB (cmd lock_settings clear-passkey). "
                        + "Si vifaa vyote vinavyounga mkono amri hii bila root. Kifaa lazima "
                        + "kiwe TAYARI kimefunguliwa na USB Debugging iwe ON.\n\nEndelea?",
                () -> run("clear_passkey")));
    }

    private void openUrl(String url, String note) {
        set(note);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            set("❌ Haikuweza kufungua kivinjari: " + e.getMessage());
        }
    }

    private void confirm(String title, String message, Runnable onYes) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ndiyo, endelea", (DialogInterface d, int w) -> onYes.run())
                .setNegativeButton("Ghairi", null)
                .show();
    }

    private void run(String action) {
        guide.setText("Inatuma " + action + "...");
        api.executeFix("simu", action, new ApiClient.Callback() {
            @Override public void onSuccess(String body) { guide.setText(ApiClient.pretty(body)); }
            @Override public void onError(String message) {
                guide.setText("❌ Server haipatikani: " + message + "\nBonyeza WASHA SERVER kwenye Tools.");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_DEVICE_ADMIN) {
            set(DeviceAdminHelper.isActive(this)
                    ? "✅ Device Admin imewezeshwa kwenye kifaa hiki."
                    : "❌ Umekataa au haijawezeshwa.");
        }
    }

    private void set(String t) { guide.setText(t); }
}
