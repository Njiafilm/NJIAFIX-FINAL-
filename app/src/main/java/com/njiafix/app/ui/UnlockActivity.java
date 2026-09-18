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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);
        setTitle("Unlock / Passkey - NjiaFix");

        spinnerBrands = findViewById(R.id.spinner_brands);
        btnClearPasskey = findViewById(R.id.btn_clear_passkey);
        btnFactoryReset = findViewById(R.id.btn_factory_reset);
        txtLogs = findViewById(R.id.txt_logs);

        String[] brands = {"Samsung", "Xiaomi", "Tecno", "Infinix", "Oppo", "Realme", "General / Nyingine"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, brands);
        spinnerBrands.setAdapter(adapter);

        btnClearPasskey.setOnClickListener(v -> {
            String selectedBrand = spinnerBrands.getSelectedItem().toString().toLowerCase().split(" ")[0];
            sendActionToServer("clear_passkey", selectedBrand);
        });

        btnFactoryReset.setOnClickListener(v -> {
            sendActionToServer("factory_reset", "general");
        });
    }

    private void sendActionToServer(String action, String brand) {
        new Thread(() -> {
            try {
                URL url = new URL("http://127.0.0.1:5555/execute-fix");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
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
                        txtLogs.setText("Hali: Imefanikiwa! Amri imetumwa kupitia ADB.");
                        Toast.makeText(this, "Imetekelezwa!", Toast.LENGTH_SHORT).show();
                    } else {
                        txtLogs.setText("Hali: Imeshindikana kutuma amri (Error: " + responseCode + ")");
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    txtLogs.setText("Hitilafu: " + e.getMessage());
                });
            }
        }).start();
    }
}
