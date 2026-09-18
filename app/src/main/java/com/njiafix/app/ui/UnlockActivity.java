package com.njiafix.app.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.njiafix.app.R;
import com.njiafix.app.api.ApiClient;

import org.json.JSONObject;

public class UnlockActivity extends AppCompatActivity {
    private TextView guide;
    private Spinner spinnerBrands;
    private TextView txtLogs;
    private ApiClient api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);
        setTitle("Unlock / Passkey");
        api = new ApiClient(this);
        guide = findViewById(R.id.tvGuide);
        spinnerBrands = findViewById(R.id.spinner_brands);
        txtLogs = findViewById(R.id.txt_logs);

        String[] brands = {"Samsung", "Xiaomi", "Tecno", "Infinix", "Oppo", "Realme", "General / nyingine"};
        spinnerBrands.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, brands));

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

        findViewById(R.id.btn_clear_passkey).setOnClickListener(v ->
                askOwnerThenExecute("clear_passkey"));
        findViewById(R.id.btn_factory_reset).setOnClickListener(v ->
                askOwnerThenExecute("factory_reset"));
    }

    private void set(String t) { guide.setText(t); }

    private void askOwnerThenExecute(String action) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad, pad, pad);

        EditText etName = new EditText(this);
        etName.setHint("Jina la mteja");
        box.addView(etName);

        EditText etReceipt = new EditText(this);
        etReceipt.setHint("Namba ya risiti / order");
        etReceipt.setInputType(InputType.TYPE_CLASS_TEXT);
        box.addView(etReceipt);

        new AlertDialog.Builder(this)
                .setTitle("Thibitisha mmiliki wa simu")
                .setMessage("Kitendo hiki ni cha kudumu na huwezi kukirudisha. Jaza taarifa za mteja kabla ya kuendelea — hii inakuhifadhi wewe kibiashara/kisheria.")
                .setView(box)
                .setPositiveButton("Endelea", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String receipt = etReceipt.getText().toString().trim();
                    if (name.isEmpty() || receipt.isEmpty()) {
                        Toast.makeText(this, "Jaza jina na namba ya risiti kwanza.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendActionToServer(action, spinnerBrands.getSelectedItem().toString().toLowerCase(), name, receipt);
                })
                .setNegativeButton("Ghairi", null)
                .show();
    }

    private void sendActionToServer(String action, String brand, String ownerName, String receipt) {
        txtLogs.setText("Inatuma amri...");
        try {
            JSONObject body = new JSONObject();
            body.put("action", action);
            body.put("brand", brand);
            body.put("owner_name", ownerName);
            body.put("receipt", receipt);

            api.post("/execute-fix", body, new ApiClient.Callback() {
                @Override public void onSuccess(String response) {
                    txtLogs.setText("Hali: " + response);
                }
                @Override public void onError(String message) {
                    txtLogs.setText("Hitilafu: " + message);
                }
            });
        } catch (Exception e) {
            txtLogs.setText("Hitilafu: " + e.getMessage());
        }
    }
}
