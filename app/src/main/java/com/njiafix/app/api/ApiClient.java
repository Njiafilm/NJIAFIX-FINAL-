package com.njiafix.app.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {
    public interface Callback {
        void onSuccess(String body);
        void onError(String message);
    }

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    private static final ExecutorService EXEC = Executors.newCachedThreadPool();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private final Context appContext;

    public ApiClient(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public void get(String path, Callback cb) {
        EXEC.execute(() -> {
            try {
                Request req = new Request.Builder()
                        .url(ServerConfig.baseUrl(appContext) + path)
                        .get().build();
                try (Response res = CLIENT.newCall(req).execute()) {
                    String body = res.body() != null ? res.body().string() : "";
                    if (res.isSuccessful()) MAIN.post(() -> cb.onSuccess(body));
                    else MAIN.post(() -> cb.onError("HTTP " + res.code() + ": " + body));
                }
            } catch (IOException e) {
                MAIN.post(() -> cb.onError(e.getMessage()));
            }
        });
    }

    public void post(String path, JSONObject json, Callback cb) {
        EXEC.execute(() -> {
            try {
                RequestBody body = RequestBody.create(json.toString(), JSON);
                Request req = new Request.Builder()
                        .url(ServerConfig.baseUrl(appContext) + path)
                        .post(body).build();
                try (Response res = CLIENT.newCall(req).execute()) {
                    String resp = res.body() != null ? res.body().string() : "";
                    if (res.isSuccessful()) MAIN.post(() -> cb.onSuccess(resp));
                    else MAIN.post(() -> cb.onError("HTTP " + res.code() + ": " + resp));
                }
            } catch (Exception e) {
                MAIN.post(() -> cb.onError(e.getMessage()));
            }
        });
    }

    public void executeFix(String category, String action, Callback cb) {
        try {
            JSONObject o = new JSONObject();
            o.put("category", category);
            o.put("action", action);
            post("/execute-fix", o, cb);
        } catch (Exception e) {
            cb.onError(e.getMessage());
        }
    }
}
