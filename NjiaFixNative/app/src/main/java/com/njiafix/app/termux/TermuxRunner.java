package com.njiafix.app.termux;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

/**
 * Huwasiliana na Termux kupitia RUN_COMMAND intent (huduma rasmi ya Termux:API /
 * Termux app - com.termux.app.RunCommandService). Hii huruhusu NjiaFix app "kuamuru"
 * Termux kutekeleza amri (mfano adb) BILA fundi kubandika (paste) chochote Termux-ni.
 *
 * MASHARTI (upande wa Termux, si NjiaFix):
 *   1. Termux (F-Droid / GitHub build - sio ile ya Play Store, imezuiwa update tangu 2021)
 *      lazima iwe imewekwa kwenye kifaa hicho hicho cha fundi.
 *   2. Faili ~/.termux/termux.properties lazima iwe na mstari:
 *          allow-external-apps=true
 *      (kisha `termux-reload-settings` au anzisha upya Termux)
 *   3. `pkg install android-tools` ndani ya Termux ili amri za adb/fastboot zifanye kazi.
 *   4. NjiaFix inahitaji ruhusa "com.termux.permission.RUN_COMMAND" (tazama AndroidManifest.xml)
 *      na mtumiaji atakiwa ku-grant ruhusa hiyo mara moja (Android inauliza automatically
 *      kwa sababu ni "dangerous permission").
 *
 * Bila hatua hizo 4 (hasa #2), Termux itakataa amri kwa usalama - hii ni tabia rasmi ya
 * Termux, si kizuizi cha NjiaFix.
 */
public final class TermuxRunner {

    private static final String TERMUX_PACKAGE = "com.termux";
    private static final String RUN_COMMAND_SERVICE = "com.termux.app.RunCommandService";
    private static final String ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND";
    private static final String TERMUX_BASH = "/data/data/com.termux/files/usr/bin/bash";
    private static final String TERMUX_HOME = "/data/data/com.termux/files/home";

    private TermuxRunner() {}

    /**
     * Tekeleza script ya shell ndani ya Termux, ukiwa "background" (bila kufungua
     * dirisha la Termux mbele ya fundi). Matokeo (stdout/stderr) hayarudishwi moja
     * kwa moja kwenye NjiaFix - kama unahitaji matokeo humu (mf. "adb devices" output
     * ionekane kwenye app), tumia runAndCapture() badala yake, ambayo huandika matokeo
     * kwenye faili na kuyasoma.
     */
    public static void runBackground(Context context, String shellScript) {
        run(context, shellScript, true, "0");
    }

    /** Kama unataka Termux ionekane mbele (session mpya) badala ya background. */
    public static void runForeground(Context context, String shellScript) {
        run(context, shellScript, false, "0");
    }

    private static void run(Context context, String shellScript, boolean background, String sessionAction) {
        try {
            Intent intent = new Intent();
            intent.setClassName(TERMUX_PACKAGE, RUN_COMMAND_SERVICE);
            intent.setAction(ACTION_RUN_COMMAND);
            intent.putExtra("com.termux.RUN_COMMAND_PATH", TERMUX_BASH);
            intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{"-c", shellScript});
            intent.putExtra("com.termux.RUN_COMMAND_WORKDIR", TERMUX_HOME);
            intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", background);
            intent.putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", sessionAction);
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent);
            else context.startService(intent);
        } catch (SecurityException e) {
            Toast.makeText(context,
                    "Termux imekataa amri: ruhusa 'allow-external-apps=true' haijawekwa " +
                    "(angalia ~/.termux/termux.properties)", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Termux haipatikani: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /** Amri za kawaida za ukarabati - zinatumika na ToolsActivity/PhoneActivity buttons. */
    public static final class Commands {
        private Commands() {}

        /** Huwasha server ya NJIAFIX (wake-lock + adb + pm2). Faili linatoka kwenye njiafix-server.zip. */
        public static final String START_SERVER =
                "/data/data/com.termux/files/home/njiafix-server/start.sh";

        public static final String ADB_DEVICES =
                "adb devices -l";

        public static final String ADB_REBOOT =
                "adb reboot";

        public static final String ADB_REBOOT_BOOTLOADER =
                "adb reboot bootloader";

        public static final String ADB_REBOOT_RECOVERY =
                "adb reboot recovery";

        /** Huhitaji kifaa cha mteja kiwe kimeshafunguliwa (unlocked) na USB Debugging ON. */
        public static final String ADB_CLEAR_SCREEN_LOCK =
                "adb shell cmd lock_settings clear-passkey";
    }
}
