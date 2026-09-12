package com.njiafix.controller;

import com.njiafix.service.DiagnosticServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class DeviceFixAgentController {

    @Autowired
    private DiagnosticServiceManager diagnosticServiceManager;

    @PostMapping("/execute-fix")
    public ResponseEntity<Map<String, Object>> executeFix(@RequestBody Map<String, String> data) {
        String deviceCategory = data.get("category"); // simu, kompyuta, decoder, camera, obd, printer, router
        String action = data.get("action");           // scan, fix, reboot, flush_dns, n.k.

        Map<String, Object> body = new LinkedHashMap<>();

        // 1. Jaribu kupitia huduma maalum za uchunguzi na "Single Click Fix"
        String diagnosticResult = diagnosticServiceManager.executeSpecialAction(deviceCategory, action);
        if (diagnosticResult != null) {
            body.put("status", "success");
            body.put("category", deviceCategory);
            body.put("action", action);
            body.put("output", diagnosticResult);
            return ResponseEntity.ok(body);
        }

        // 2. Kama sio huduma maalum, tumia mfumo wa amri za OS (ProcessBuilder)
        List<String> cmd = null;

        if ("simu".equals(deviceCategory)) {
            if ("reboot".equals(action)) {
                cmd = List.of("adb", "reboot");
            } else if ("clear_cache".equals(action)) {
                cmd = List.of("adb", "shell", "pm", "trim-caches", "999G");
            }
        } else if ("kompyuta".equals(deviceCategory)) {
            if ("flush_dns".equals(action)) {
                cmd = List.of("ipconfig", "/flushdns");
            } else if ("reset_network".equals(action)) {
                cmd = List.of("netsh", "winsock", "reset");
            }
        } else if ("decoder".equals(deviceCategory)) {
            if ("reboot_sat".equals(action)) {
                // Weka amri au mantiki ya decoder hapa
            }
        }

        if (cmd == null) {
            body.put("status", "error");
            body.put("message", "Amri au kategoria haijatambuliwa kwa kifaa hiki.");
            return ResponseEntity.badRequest().body(body);
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                body.put("status", "error");
                body.put("message", "Amri imeshindwa na exit code: " + exitCode);
                body.put("output", output.toString());
                return ResponseEntity.status(500).body(body);
            }

            body.put("status", "success");
            body.put("output", output.toString());
            return ResponseEntity.ok(body);

        } catch (Exception e) {
            body.put("status", "error");
            body.put("message", e.getMessage());
            return ResponseEntity.status(500).body(body);
        }
    }
}
