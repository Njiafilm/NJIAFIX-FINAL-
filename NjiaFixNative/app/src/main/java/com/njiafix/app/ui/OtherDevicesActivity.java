package com.njiafix.app.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.njiafix.app.R;
import com.njiafix.app.api.ApiClient;

import org.json.JSONException;
import org.json.JSONObject;

public class OtherDevicesActivity extends AppCompatActivity {
    private ApiClient api;
    private TextView result;
    private TextInputEditText etIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        setTitle("Vifaa vingine");
        api = new ApiClient(this);
        result = findViewById(R.id.tvOtherResult);
        etIp = findViewById(R.id.etIp);

        // Vifaa vya mtandao (vinahitaji IP)
        findViewById(R.id.btnPrinter).setOnClickListener(v -> diag("/api/diagnose/printer/network/"));
        findViewById(R.id.btnCamera).setOnClickListener(v -> diag("/api/diagnose/camera/"));
        findViewById(R.id.btnRouter).setOnClickListener(v -> diag("/api/diagnose/router/"));
        findViewById(R.id.btnPc).setOnClickListener(v -> diag("/api/diagnose/pc/"));
        findViewById(R.id.btnTv).setOnClickListener(v -> diag("/api/diagnose/tv/"));
        findViewById(R.id.btnObd).setOnClickListener(v -> diag("/api/diagnose/obd/"));
        findViewById(R.id.btnModbus).setOnClickListener(v -> diag("/api/diagnose/modbus/"));
        findViewById(R.id.btnObdClear).setOnClickListener(v -> confirmObdClear());

        // Hazihitaji IP
        findViewById(R.id.btnLocal).setOnClickListener(v -> call("/api/diagnose/local"));
        findViewById(R.id.btnUsb).setOnClickListener(v -> call("/api/usb"));
    }

    private String ip() {
        return etIp.getText() != null ? etIp.getText().toString().trim() : "";
    }

    private void diag(String pathPrefix) {
        String ip = ip();
        if (ip.isEmpty()) {
            result.setText("Weka IP kwanza");
            return;
        }
        call(pathPrefix + ip);
    }

    private void call(String path) {
        result.setText("Inachunguza...");
        api.get(path, new ApiClient.Callback() {
            @Override public void onSuccess(String body) { result.setText(ApiClient.pretty(body)); }
            @Override public void onError(String message) { result.setText("❌ Server haipatikani: " + message); }
        });
    }

    private void confirmObdClear() {
        final String ip = ip();
        if (ip.isEmpty()) {
            result.setText("Weka IP ya adapta ELM327 kwanza");
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Futa codes za gari")
                .setMessage("Hii inafuta codes (DTC) na freeze-frame kwenye kompyuta ya gari (" + ip
                        + "). Andika/hifadhi codes kwanza kwa 'Gari (OBD)'. Injini izimwe, ignition ON.\n\nEndelea?")
                .setPositiveButton("Ndiyo, futa", (DialogInterface d, int w) -> obdClear(ip))
                .setNegativeButton("Ghairi", null)
                .show();
    }

    private void obdClear(String ip) {
        try {
            JSONObject j = new JSONObject();
            j.put("category", "gari");
            j.put("action", "obd_clear_dtc");
            j.put("host", ip);
            result.setText("Inafuta codes...");
            api.post("/execute-fix", j, new ApiClient.Callback() {
                @Override public void onSuccess(String body) { result.setText(ApiClient.pretty(body)); }
                @Override public void onError(String message) { result.setText("❌ Server haipatikani: " + message); }
            });
        } catch (JSONException e) {
            result.setText("❌ " + e.getMessage());
        }
    }
}
