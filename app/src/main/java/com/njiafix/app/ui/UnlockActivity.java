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
    private static final String SERVER_URL = "http://10.0.2.2:3000/execute-fix"; 
    private static final String API_KEY = "njiafix_secret_key_2025";

    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_unlock);  
        spinnerBrands = findViewById(R.id.spinner_brands);  
        btnClearPasskey = findViewById(R.id.btn_clear_passkey);  
        btnFactoryReset = findViewById(R.id.btn_factory_reset);  
        txtLogs = findViewById(R.id.txt_logs);  
        String[] brands = {"Samsung", "Xiaomi", "Tecno", "Infinix", "Oppo", "Realme", "General"};  
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, brands);  
        spinnerBrands.setAdapter(adapter);  
        btnClearPasskey.setOnClickListener(v -> sendActionToServer("clear_passkey", spinnerBrands.getSelectedItem().toString()));  
        btnFactoryReset.setOnClickListener(v -> sendActionToServer("factory_reset", spinnerBrands.getSelectedItem().toString()));
    }  

    private void sendActionToServer(String action, String brand) {  
        txtLogs.setText("Inatuma...");
        new Thread(() -> {  
            try {  
                HttpURLConnection conn = (HttpURLConnection) new URL(SERVER_URL).openConnection();  
                conn.setRequestMethod("POST");  
                conn.setRequestProperty("Content-Type", "application/json");  
                conn.setRequestProperty("x-api-key", API_KEY);  
                conn.setDoOutput(true);  
                new JSONObject().put("action", action).put("brand", brand).toString().getBytes();
                try (OutputStream os = conn.getOutputStream()) { os.write(new JSONObject().put("action", action).put("brand", brand).toString().getBytes()); }
                runOnUiThread(() -> { txtLogs.setText(conn.getResponseCode() == 200 ? "✅ Imefanikiwa!" : "❌ Kosa: " + conn.getResponseCode()); });
            } catch (Exception e) { runOnUiThread(() -> txtLogs.setText("❌ " + e.getMessage())); }  
        }).start();  
    }
}
