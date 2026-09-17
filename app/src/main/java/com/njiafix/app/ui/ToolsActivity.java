package com.njiafix.app.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.njiafix.app.R;
import com.njiafix.app.api.ApiClient;
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
        api = new ApiClient(this);

        findViewById(R.id.btnAdb).setOnClickListener(v -> set(
                "ADB + Fastboot – Simu zote (OFFLINE)\n\n" +
                "Platform Tools + USB Debugging.\n" +
                "Tumia buttons chini kwa amri za haraka."));
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

        // Njia ya zamani: amri inapitia server yetu (inahitaji ApiClient iwe imeunganishwa
        // na huduma inayofanya kazi na adb upande wa server).
        findViewById(R.id.btnAdbDevices).setOnClickListener(v -> run("adb_devices"));
        findViewById(R.id.btnAdbReboot).setOnClickListener(v -> run("reboot"));
        findViewById(R.id.btnAdbFastboot).setOnClickListener(v -> run("reboot_bootloader"));

        // Njia mpya: buttons zake mwenyewe, zinatuma amri moja kwa moja kwa Termux
        // (RUN_COMMAND intent) - haihitaji server, wala fundi kubandika chochote.
        termuxStatus = findViewById(R.id.tvTermuxStatus);
        findViewById(R.id.btnTermuxDevices).setOnClickListener(v ->
                runViaTermux("devices", TermuxRunner.Commands.ADB_DEVICES));
        findViewById(R.id.btnTermuxReboot).setOnClickListener(v ->
                runViaTermux("reboot", TermuxRunner.Commands.ADB_REBOOT));
        findViewById(R.id.btnTermuxFastboot).setOnClickListener(v ->
                runViaTermux("reboot bootloader", TermuxRunner.Commands.ADB_REBOOT_BOOTLOADER));
        findViewById(R.id.btnTermuxRecovery).setOnClickListener(v ->
                runViaTermux("reboot recovery", TermuxRunner.Commands.ADB_REBOOT_RECOVERY));

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
            @Override public void onSuccess(String body) { guide.setText("✅ " + body); }
            @Override public void onError(String message) { guide.setText("❌ " + message); }
        });
    }

    private void runViaTermux(String label, String shellCommand) {
        termuxStatus.setText("📟 Termux: " + label + " imetumwa (background)...");
        TermuxRunner.runBackground(this, shellCommand);
    }
}
