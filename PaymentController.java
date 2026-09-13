package com.njiafix.controller;

import com.njiafix.model.Payment;
import com.njiafix.repository.PaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {
    private final PaymentRepository paymentRepository;
    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmPayment(@RequestBody Map<String, String> payload) {
        Map<String, Object> body = new LinkedHashMap<>();
        String companyNumber = payload.get("company_number");
        String paymentRef = payload.get("payment_ref");
        String customerPhone = payload.get("customer_phone");
        if (companyNumber == null || companyNumber.isBlank()) {
            body.put("status", "error");
            body.put("message", "Namba ya kampuni inahitajika.");
            return ResponseEntity.badRequest().body(body);
        }
        if (paymentRef == null || paymentRef.isBlank() || paymentRef.trim().length() < 5) {
            body.put("status", "error");
            body.put("message", "Kumbukumbu namba si sahihi (angalau 5).");
            return ResponseEntity.badRequest().body(body);
        }
        paymentRef = paymentRef.trim();
        companyNumber = companyNumber.trim();
        if (paymentRepository.existsByPaymentRef(paymentRef)) {
            body.put("status", "error");
            body.put("message", "Kumbukumbu hii tayari imetumika.");
            return ResponseEntity.badRequest().body(body);
        }
        Payment payment = new Payment(companyNumber, paymentRef, customerPhone);
        Payment saved = paymentRepository.save(payment);
        body.put("status", "success");
        body.put("message", "Malipo yamethibitishwa.");
        body.put("payment_id", saved.getId());
        body.put("company_number", saved.getCompanyNumber());
        body.put("payment_ref", saved.getPaymentRef());
        body.put("customer_phone", saved.getCustomerPhone());
        body.put("created_at", saved.getCreatedAt().toString());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/check/{ref}")
    public ResponseEntity<Map<String, Object>> checkPayment(@PathVariable String ref) {
        Map<String, Object> body = new LinkedHashMap<>();
        return paymentRepository.findByPaymentRef(ref.trim())
            .map(p -> {
                body.put("status", "success");
                body.put("confirmed", true);
                body.put("company_number", p.getCompanyNumber());
                body.put("payment_ref", p.getPaymentRef());
                return ResponseEntity.ok(body);
            })
            .orElseGet(() -> {
                body.put("status", "success");
                body.put("confirmed", false);
                return ResponseEntity.ok(body);
            });
    }
}
