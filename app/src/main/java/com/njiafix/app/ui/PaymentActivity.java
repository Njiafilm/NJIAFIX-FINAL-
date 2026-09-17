package com.njiafix.app.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.njiafix.app.R;
import com.njiafix.app.api.ApiClient;

import org.json.JSONObject;

public class PaymentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        setTitle("Malipo");

        TextInputEditText etCompany = findViewById(R.id.etCompany);
        TextInputEditText etRef = findViewById(R.id.etRef);
        TextInputEditText etPhone = findViewById(R.id.etPhone);
        TextView tv = findViewById(R.id.tvPayResult);
        ApiClient api = new ApiClient(this);

        findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            String company = etCompany.getText() != null ? etCompany.getText().toString().trim() : "";
            String ref = etRef.getText() != null ? etRef.getText().toString().trim() : "";
            String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
            if (company.isEmpty() || ref.length() < 5) {
                tv.setText("❌ Weka kampuni + kumbukumbu (5+)");
                return;
            }
            tv.setText("Inathibitisha...");
            try {
                JSONObject o = new JSONObject();
                o.put("company_number", company);
                o.put("payment_ref", ref);
                o.put("customer_phone", phone.isEmpty() ? null : phone);
                api.post("/api/payment/confirm", o, new ApiClient.Callback() {
                    @Override public void onSuccess(String body) {
                        tv.setText("✅ Malipo yamethibitishwa\n" + body);
                    }
                    @Override public void onError(String message) {
                        // Offline fallback – still record locally message
                        tv.setText("⚠️ Server haikujibu. Angalia IP.\n" + message);
                    }
                });
            } catch (Exception e) {
                tv.setText(e.getMessage());
            }
        });
    }
}
