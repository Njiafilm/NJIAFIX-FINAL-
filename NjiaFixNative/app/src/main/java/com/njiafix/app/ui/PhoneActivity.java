package com.njiafix.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.njiafix.app.R;
import com.njiafix.app.api.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

public class PhoneActivity extends AppCompatActivity {
    private ApiClient api;
    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        setTitle("Simu");
        api = new ApiClient(this);
        result = findViewById(R.id.tvResult);

        findViewById(R.id.btnDiagnose).setOnClickListener(v -> listDevices());
        findViewById(R.id.btnReboot).setOnClickListener(v -> runFix("reboot"));
        findViewById(R.id.btnCache).setOnClickListener(v -> runFix("clear_cache"));
        findViewById(R.id.btnWifi).setOnClickListener(v -> runFix("toggle_wifi"));
        findViewById(R.id.btnInfo).setOnClickListener(v -> runFix("adb_info"));
        findViewById(R.id.btnRecovery).setOnClickListener(v -> runFix("reboot_recovery"));
        findViewById(R.id.btnPayment).setOnClickListener(v ->
                startActivity(new Intent(this, PaymentActivity.class)));
        findViewById(R.id.btnUnlock).setOnClickListener(v ->
                startActivity(new Intent(this, UnlockActivity.class)));
        findViewById(R.id.btnTools).setOnClickListener(v ->
                startActivity(new Intent(this, ToolsActivity.class)));
    }

    private void listDevices() {
        result.setText("Inatafuta vifaa...");
        api.get("/api/devices", new ApiClient.Callback() {
            @Override public void onSuccess(String body) {
                try {
                    JSONObject o = new JSONObject(body);
                    if (!o.optBoolean("ok", true)) {
                        result.setText("❌ " + ApiClient.text(body));
                        return;
                    }
                    JSONArray arr = o.optJSONArray("devices");
                    if (arr == null || arr.length() == 0) {
                        result.setText("Hakuna kifaa kilichounganishwa. Washa USB Debugging / unganisha kwa Wi-Fi.");
                        return;
                    }
                    StringBuilder sb = new StringBuilder("✅ Vifaa:\n");
                    for (int i = 0; i < arr.length(); i++) {
                        String serial = arr.getString(i);
                        sb.append("• ").append(serial).append("\n");
                        diagnose(serial);
                    }
                    result.setText(sb.toString());
                } catch (Exception e) {
                    result.setText(body);
                }
            }
            @Override public void onError(String message) {
                result.setText("❌ " + message + "\nHakikisha server inaendesha.");
            }
        });
    }

    private void diagnose(String serial) {
        api.get("/api/diagnose/" + serial, new ApiClient.Callback() {
            @Override public void onSuccess(String body) {
                try {
                    JSONObject o = new JSONObject(body);
                    StringBuilder sb = new StringBuilder(result.getText());
                    sb.append("\n--- ").append(serial).append(" ---\n");
                    if (!o.optBoolean("ok", true)) {
                        sb.append("❌ ").append(ApiClient.text(body)).append("\n");
                        result.setText(sb.toString());
                        return;
                    }
                    JSONArray issues = o.optJSONArray("issues");
                    if (issues == null || issues.length() == 0) sb.append("Hakuna tatizo lililothibitishwa.\n");
                    else for (int i = 0; i < issues.length(); i++) {
                        JSONObject is = issues.getJSONObject(i);
                        sb.append("• ").append(is.optString("code")).append(": ")
                          .append(is.optString("description")).append("\n");
                    }
                    result.setText(sb.toString());
                } catch (Exception e) {
                    result.setText(result.getText() + "\n" + body);
                }
            }
            @Override public void onError(String message) {
                result.append("\nDiagnose error: " + message);
            }
        });
    }

    private void runFix(String action) {
        result.setText("Inatuma: " + action + "...");
        api.executeFix("simu", action, new ApiClient.Callback() {
            @Override public void onSuccess(String body) {
                result.setText(ApiClient.pretty(body));
            }
            @Override public void onError(String message) {
                result.setText("❌ Server haipatikani: " + message);
            }
        });
    }
}
