package com.njiafix.app.api;

import android.content.Context;
import android.content.SharedPreferences;

public final class ServerConfig {
    private static final String PREFS = "njiafix_prefs";
    private static final String KEY_HOST = "server_host";
    private static final String DEFAULT_HOST = "127.0.0.1"; // server ya Termux ndani ya simu hii (PC: weka IP yake kwenye Mipangilio)

    private ServerConfig() {}

    public static String getHost(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_HOST, DEFAULT_HOST);
    }

    public static void setHost(Context ctx, String host) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_HOST, host.trim()).apply();
    }

    public static String baseUrl(Context ctx) {
        return "http://" + getHost(ctx) + ":5555";
    }
}
