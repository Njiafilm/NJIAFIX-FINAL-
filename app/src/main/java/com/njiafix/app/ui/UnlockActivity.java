package com.njiafix.app.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.njiafix.app.R;

public class UnlockActivity extends AppCompatActivity {
    private TextView guide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);
        setTitle("Unlock / Passkey");
        guide = findViewById(R.id.tvGuide);

        findViewById(R.id.btnGoogle).setOnClickListener(v -> set(
                "🌐 Google Find My Device\n\n" +
                "1. https://www.google.com/android/find\n" +
                "2. Ingia account ya mteja\n" +
                "3. Secure device / Lock au Erase\n\n" +
                "Inahitaji INTANETI."));
        findViewById(R.id.btnSamsung).setOnClickListener(v -> set(
                "📱 Samsung Find My Mobile\n\n" +
                "1. https://findmymobile.samsung.com\n" +
                "2. Unlock my screen\n\n" +
                "Inahitaji INTANETI + Find My Mobile ON."));
        findViewById(R.id.btnFactory).setOnClickListener(v -> set(
                "⚠️ Factory Reset (OFFLINE)\n\n" +
                "Inafuta DATA YOTE!\n\n" +
                "1. Zima simu\n" +
                "2. Samsung: Vol Up + Power\n" +
                "   Xiaomi/Tecno/Infinix: Vol Up + Power\n" +
                "   Oppo/Realme: Vol Down + Power\n" +
                "3. Wipe data/factory reset → Yes\n" +
                "4. Reboot\n\n" +
                "FRP inaweza kuonekana baadaye."));
        findViewById(R.id.btnPkSettings).setOnClickListener(v -> set(
                "⚙️ Passkey – Settings (OFFLINE)\n\n" +
                "Simu IMEFUNGULIWA:\n" +
                "Settings → Passwords / Passkeys → Delete"));
        findViewById(R.id.btnPkGoogle).setOnClickListener(v -> set(
                "🌐 Passkey – Google\n\n" +
                "myaccount.google.com/security → Passkeys → Delete\n" +
                "Inahitaji intaneti."));
        findViewById(R.id.btnPkAdb).setOnClickListener(v -> set(
                "💻 Passkey – ADB (OFFLINE)\n\n" +
                "adb shell cmd lock_settings clear-passkey\n\n" +
                "Si brand zote. USB Debugging ON."));
    }

    private void set(String t) { guide.setText(t); }
}
