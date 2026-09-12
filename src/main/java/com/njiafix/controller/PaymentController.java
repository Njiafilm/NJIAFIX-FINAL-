package com.njiafix.controller;

import com.njiafix.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> payload) {
        String companyNumber = (String) payload.get("companyNumber");
        String phoneNumber = (String) payload.get("phoneNumber");
        
        // Kusoma kiasi kwa usalama
        double amount = 0.0;
        if (payload.get("amount") != null) {
            amount = Double.parseDouble(payload.get("amount").toString());
        }

        boolean isSuccess = paymentService.verifyAndMarkAsPaid(companyNumber, phoneNumber, amount);

        if (isSuccess) {
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Malipo yamethibitishwa na kuwekwa kuwa yamelipwa (PAID)."
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "FAILED",
                "message", "Imeshindwa kusawazisha malipo. Hakiki namba ya kampuni."
            ));
        }
    }
}
