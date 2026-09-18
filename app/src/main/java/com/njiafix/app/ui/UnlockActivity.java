package com.njiafix.app.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.njiafix.app.R;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UnlockActivity extends AppCompatActivity {

    private Spinner spinnerBrands;
    private Button btnClearPasskey, btnFactoryReset;
    private TextView txtLogs;

    // BADILISHA HII: Weka IP ya kompyuta yako (k.m., 192.168.1.10) au 10.0.2.2 kwa Emulator
    private static final String SERVER_URL = "http://10.0.2.2:3000/execute-fix";
    private static final String API_KEY = "njiafix_secret_key_2025";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);

        // 1. Initialize Views
        spinnerBrands = findViewById(R.id.spinner_brands);
        btnClearPasskey = findViewById(R.id.btn_clear_passkey);
        btnFactoryReset = findViewById(R.id.btn_factory_reset);
        txtLogs = findViewById(R.id.txt_logs);

        // 2. Setup Spinner
        String[] brands = {"Samsung", "Xiaomi", "Tecno", "Infinix", "Oppo", "Realme", "General / Nyingine"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, brands);
        spinnerBrands.setAdapter(adapter);

        // 3. Setup Click Listeners
        btnClearPasskey.setOnClickListener(v -> {
            String selectedBrand = spinnerBrands.getSelectedItem().toString();
            sendActionToServer("clear_passkey", selectedBrand);
        });

        btnFactoryReset.setOnClickListener(v -> {
            String selectedBrand = spinnerBrands.getSelectedItem().toString();
            sendActionToServer("factory_reset", selectedBrand);
        });
    }

    private void sendActionToServer(String action, String brand) {
        txtLogs.setText("Inatuma amri: " + action + " kwa " + brand + "...");

        new Thread(() -> {
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("x-api-key", API_KEY);
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("action", action);
                jsonParam.put("brand", brand);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonParam.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                runOnUiThread(() -> {
                    if (responseCode == 200) {
                        txtLogs.setText("Hali: Imefanikiwa! Amri imepokelewa na server.");
                        Toast.makeText(UnlockActivity.this, "Imetekelezwa: " + action, Toast.LENGTH_SHORT).show();
                    } else {
                        txtLogs.setText("Hali: Imeshindikana (Error Code: " + responseCode + ")");
                        Toast.makeText(UnlockActivity.this, "Kosa la Server: " + responseCode, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    txtLogs.setText("Hitilafu ya Mtandao: " + e.getMessage());
                    Toast.makeText(UnlockActivity.this, "Hakuna muunganisho na server", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
