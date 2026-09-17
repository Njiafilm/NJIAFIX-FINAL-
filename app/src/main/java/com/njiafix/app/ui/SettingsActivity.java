package com.njiafix.app.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.njiafix.app.R;
import com.njiafix.app.api.ServerConfig;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Mipangilio");

        TextInputEditText et = findViewById(R.id.etServer);
        et.setText(ServerConfig.getHost(this));

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            String host = et.getText() != null ? et.getText().toString().trim() : "";
            if (host.isEmpty()) {
                Toast.makeText(this, "Weka IP", Toast.LENGTH_SHORT).show();
                return;
            }
            ServerConfig.setHost(this, host);
            Toast.makeText(this, "Imehifadhiwa: " + ServerConfig.baseUrl(this), Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
