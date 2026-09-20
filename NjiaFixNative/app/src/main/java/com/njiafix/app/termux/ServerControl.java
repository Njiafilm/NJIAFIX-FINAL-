package com.njiafix.app.termux;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.njiafix.app.api.ApiClient;
import com.njiafix.app.api.ServerConfig;

/**
 * Kuwasha na kukagua server ya NJIAFIX (Termux). "Imewaka" inaonyeshwa TU baada ya
 * /health kujibu kweli - si kwa sababu amri ilitumwa.
 */
public final class ServerControl {

    public interface Listener {
        void onStatus(String text);
    }

    private static final String TERMUX_PERMISSION = "com.termux.permission.RUN_COMMAND";
    private static final int REQ_TERMUX = 4242;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private ServerControl() {}

    public static boolean hasPermission(Context ctx) {
        return ContextCompat.checkSelfPermission(ctx, TERMUX_PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Inaomba ruhusa ya Termux kama bado haijatolewa. true = tayari imetolewa. */
    public static boolean ensurePermission(Activity activity) {
        if (hasPermission(activity)) return true;
        ActivityCompat.requestPermissions(activity, new String[]{TERMUX_PERMISSION}, REQ_TERMUX);
        return false;
    }

    private static boolean isLocalHost(Context ctx) {
        String h = ServerConfig.getHost(ctx);
        return "127.0.0.1".equals(h) || "localhost".equalsIgnoreCase(h);
    }

    public static void start(Activity activity, Listener l) {
        if (!isLocalHost(activity)) {
            l.onStatus("ℹ️ Server imewekwa kwenye " + ServerConfig.getHost(activity)
                    + " (si Termux ya simu hii). Weka 127.0.0.1 kwenye Mipangilio kutumia server ya Termux.");
            return;
        }
        if (!ensurePermission(activity)) {
            l.onStatus("🔐 Toa ruhusa ya Termux kwenye dirisha lililotokea, kisha bonyeza tena.");
            return;
        }
        l.onStatus("🚀 Inawasha server kupitia Termux...");
        TermuxRunner.runBackground(activity, TermuxRunner.Commands.START_SERVER);
        waitForServer(activity.getApplicationContext(), l, 0);
    }

    public static void check(Context ctx, Listener l) {
        final String base = ServerConfig.baseUrl(ctx);
        l.onStatus("🔎 Inakagua " + base + " ...");
        new ApiClient(ctx).get("/health", new ApiClient.Callback() {
            @Override public void onSuccess(String body) {
                l.onStatus("🟢 Server inafanya kazi: " + base);
            }
            @Override public void onError(String message) {
                l.onStatus("🔴 Server haipatikani: " + base + " (" + message + ")");
            }
        });
    }

    private static void waitForServer(Context ctx, Listener l, int attempt) {
        new ApiClient(ctx).get("/health", new ApiClient.Callback() {
            @Override public void onSuccess(String body) {
                l.onStatus("✅ Server inafanya kazi: " + ServerConfig.baseUrl(ctx));
            }
            @Override public void onError(String message) {
                if (attempt >= 10) {
                    l.onStatus("❌ Server haikuwaka. Angalia: Termux imewekwa, allow-external-apps=true "
                            + "kwenye ~/.termux/termux.properties, na ~/njiafix-server/start.sh ipo.");
                    return;
                }
                l.onStatus("⏳ Inasubiri server... (" + (attempt + 1) + "/10)");
                MAIN.postDelayed(() -> waitForServer(ctx, l, attempt + 1), 2000);
            }
        });
    }
}
