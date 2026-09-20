package com.njiafix.app.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.njiafix.app.R;
import com.njiafix.app.api.ApiClient;
import com.njiafix.app.termux.ServerControl;
import com.njiafix.app.termux.TermuxRunner;

public class ToolsActivity extends AppCompatActivity {
    private TextView guide;
    private TextView termuxStatus;
    private ApiClient api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);
        setTitle("Software Tools");
        guide = findViewById(R.id.tvToolGuide);
        termuxStatus = findViewById(R.id.tvTermuxStatus);
        api = new ApiClient(this);

        findViewById(R.id.btnAdb).setOnClickListener(v -> set(
                "ADB + Fastboot – Simu zote (OFFLINE)\n\n" +
                "Platform Tools + USB Debugging.\n" +
                "Tumia vitufe vilivyo chini kwa amri halisi."));
        findViewById(R.id.btnOdin).setOnClickListener(v -> set(
                "Odin – Samsung zote (OFFLINE)\n\n" +
                "Download Mode:\n" +
                "• A/M: Vol Down+Vol Up + USB\n" +
                "• S/Note: Vol Down+Bixby+Power\n\n" +
                "Flash BL+AP+CP+CSC ya MODEL sahihi.\n" +
                "FRP haiondoki kwa flash peke yake."));
        findViewById(R.id.btnSp).setOnClickListener(v -> set(
                "SP Flash Tool – MTK (OFFLINE)\n\n" +
                "Tecno/Infinix/Itel.\n" +
                "1. VCOM driver\n" +
                "2. Scatter file\n" +
                "3. Download Only (salama)\n" +
                "Format All = hatari (IMEI)"));
        findViewById(R.id.btnMi).setOnClickListener(v -> set(
                "Mi Flash – Xiaomi (SEHEMU)\n\n" +
                "Unlock bootloader = intaneti + waiting.\n" +
                "Baada ya unlock → fastboot flash offline."));
        findViewById(R.id.btnMiracle).setOnClickListener(v -> set(
                "Miracle/Thunder Box (OFFLINE)\n\n" +
                "FRP / Unlock / Test-point.\n" +
                "Thibitisha umiliki kwanza."));
        findViewById(R.id.btnUmt).setOnClickListener(v -> set(
                "UMT / DFT / Chimera (OFFLINE)\n\n" +
                "Chimera ≈ Samsung\nUMT/DFT ≈ MTK+\n" +
                "IMEI: restore asli tu (sheria)."));
        findViewById(R.id.btnSamfw).setOnClickListener(v -> set(
                "SamFw – Samsung FRP\n\n" +
                "Baadhi online, baadhi offline.\n" +
                "Enable ADB → tumia NjiaFix commands."));

        // Amri halisi: zinapitia server, na jibu linaonyesha ✅/❌ kulingana na matokeo ya kweli.
        findViewById(R.id.btnAdbDevices).setOnClickListener(v -> run("adb_devices"));
        findViewById(R.id.btnAdbReboot).setOnClickListener(v -> run("reboot"));
        findViewById(R.id.btnAdbFastboot).setOnClickListener(v -> run("reboot_bootloader"));
        findViewById(R.id.btnAdbRecovery).setOnClickListener(v -> run("reboot_recovery"));
        findViewById(R.id.btnAdbInfo).setOnClickListener(v -> run("adb_info"));
        findViewById(R.id.btnAdbDiagnose).setOnClickListener(v -> run("diagnose"));
        findViewById(R.id.btnFastbootDevices).setOnClickListener(v -> run("fastboot_devices"));

        // Server ya Termux
        findViewById(R.id.btnStartServer).setOnClickListener(v ->
                ServerControl.start(this, s -> termuxStatus.setText(s)));
        findViewById(R.id.btnServerStatus).setOnClickListener(v ->
                ServerControl.check(this, s -> termuxStatus.setText(s)));

        // Hatua nyeti (inaondoa screen lock) - inahitaji uthibitisho wa umiliki kabla ya kutuma.
        findViewById(R.id.btnTermuxClearLock).setOnClickListener(v -> confirmClearLock());
    }

    private void confirmClearLock() {
        new AlertDialog.Builder(this)
                .setTitle("Thibitisha umiliki")
                .setMessage("Hakikisha una risiti/uthibitisho wa umiliki wa kifaa hiki kabla ya " +
                        "kuondoa screen lock. Kifaa lazima kiwe TAYARI kimefunguliwa na USB " +
                        "Debugging iwe ON.\n\nEndelea?")
                .setPositiveButton("Ndiyo, endelea", (DialogInterface d, int w) ->
                        runViaTermux("clear screen lock", TermuxRunner.Commands.ADB_CLEAR_SCREEN_LOCK))
                .setNegativeButton("Ghairi", null)
                .show();
    }

    private void set(String t) { guide.setText(t); }

    private void run(String action) {
        guide.setText("Inatuma " + action + "...");
        api.executeFix("simu", action, new ApiClient.Callback() {
            @Override public void onSuccess(String body) { guide.setText(ApiClient.pretty(body)); }
            @Override public void onError(String message) {
                guide.setText("❌ Server haipatikani: " + message + "\nBonyeza 🟢 WASHA SERVER.");
            }
        });
    }

    /** Amri ya moja kwa moja kwa Termux: matokeo HAYARUDI kwenye app, kwa hiyo hayajathibitishwa. */
    private void runViaTermux(String label, String shellCommand) {
        if (!ServerControl.ensurePermission(this)) {
            termuxStatus.setText("🔐 Toa ruhusa ya Termux kwenye dirisha lililotokea, kisha jaribu tena.");
            return;
        }
        termuxStatus.setText("📟 Termux: " + label + " imetumwa. Matokeo hayarudi kwenye app (hayajathibitishwa).");
        TermuxRunner.runBackground(this, shellCommand);
    }
}
