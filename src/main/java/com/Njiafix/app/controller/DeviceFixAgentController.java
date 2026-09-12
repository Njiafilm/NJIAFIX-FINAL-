package com.njiafix.controller;

import com.njiafix.service.CommandRegistryService;
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

    @Autowired
    private CommandRegistryService commandRegistryService;

    @PostMapping("/execute-fix")
    public ResponseEntity<Map<String, Object>> executeFix(@RequestBody Map<String, String> data) {
        String deviceCategory = data.get("category");
        String action = data.get("action");

        Map<String, Object> body = new LinkedHashMap<>();

        if (deviceCategory == null || action == null) {
            body.put("status", "error");
            body.put("message", "Tafadhali toa 'category' na 'action'.");
            return ResponseEntity.badRequest().body(body);
        }

        // 1. Kwanza angalia kama ni huduma maalum za uchunguzi (Diagnostic Services)
        String diagnosticResult = diagnosticServiceManager.executeSpecialAction(deviceCategory, action);
        if (diagnosticResult != null) {
            body.put("status", "success");
            body.put("category", deviceCategory);
            body.put("action", action);
            body.put("output", diagnosticResult);
            return ResponseEntity.ok(body);
        }

        // 2. Tafuta amri kutoka kwenye Registry ya amri 500+
        List<String> cmd = commandRegistryService.getCommand(deviceCategory, action);

        if (cmd == null) {
            body.put("status", "error");
            body.put("message", "Amri au kategoria haijatambuliwa kwenye mfumo.");
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
