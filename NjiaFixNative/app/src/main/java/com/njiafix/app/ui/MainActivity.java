package com.njiafix.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.njiafix.app.R;
import com.njiafix.app.api.ServerConfig;
import com.njiafix.app.termux.ServerControl;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("NjiaFix");

        TextView tv = findViewById(R.id.tvServer);
        tv.setText("Server: " + ServerConfig.baseUrl(this));

        findViewById(R.id.btnStartServer).setOnClickListener(v ->
                ServerControl.start(this, s -> tv.setText(s)));
        findViewById(R.id.btnPhone).setOnClickListener(v ->
                startActivity(new Intent(this, PhoneActivity.class)));
        findViewById(R.id.btnOther).setOnClickListener(v ->
                startActivity(new Intent(this, OtherDevicesActivity.class)));
        findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView tv = findViewById(R.id.tvServer);
        if (tv != null) ServerControl.check(this, s -> tv.setText(s));
    }
}
