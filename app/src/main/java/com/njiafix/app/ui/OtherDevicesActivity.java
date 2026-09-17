package com.njiafix.app.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.njiafix.app.R;
import com.njiafix.app.api.ApiClient;

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

        findViewById(R.id.btnPrinter).setOnClickListener(v -> diag("/api/diagnose/printer/network/"));
        findViewById(R.id.btnCamera).setOnClickListener(v -> diag("/api/diagnose/camera/"));
        findViewById(R.id.btnRouter).setOnClickListener(v -> diag("/api/diagnose/router/"));
        findViewById(R.id.btnPc).setOnClickListener(v -> diag("/api/diagnose/pc/"));
    }

    private void diag(String pathPrefix) {
        String ip = etIp.getText() != null ? etIp.getText().toString().trim() : "";
        if (ip.isEmpty()) {
            result.setText("Weka IP kwanza");
            return;
        }
        result.setText("Inachunguza " + ip + "...");
        api.get(pathPrefix + ip, new ApiClient.Callback() {
            @Override public void onSuccess(String body) { result.setText(body); }
            @Override public void onError(String message) { result.setText("❌ " + message); }
        });
    }
}
