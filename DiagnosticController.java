package com.njiafix.controller;

import com.njiafix.model.DetectedIssue;
import com.njiafix.service.DeviceDetectionService;
import com.njiafix.service.DiagnosticService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class DiagnosticController {

    private final DeviceDetectionService deviceDetectionService;
    private final DiagnosticService diagnosticService;

    public DiagnosticController(DeviceDetectionService deviceDetectionService,
                                 DiagnosticService diagnosticService) {
        this.deviceDetectionService = deviceDetectionService;
        this.diagnosticService = diagnosticService;
    }

    // Orodha ya vifaa vilivyounganishwa sasa hivi
    @GetMapping("/api/devices")
    public ResponseEntity<Map<String, Object>> listDevices() {
        Map<String, Object> body = new LinkedHashMap<>();
        try {
            List<String> devices = deviceDetectionService.listConnectedDevices();
            body.put("status", "success");
            body.put("devices", devices);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            body.put("status", "error");
            body.put("message", e.getMessage());
            return ResponseEntity.status(500).body(body);
        }
    }

    // Uchunguzi wa kifaa maalum - inarejesha matatizo yaliyogunduliwa
    @GetMapping("/api/diagnose/{serial}")
    public ResponseEntity<Map<String, Object>> diagnose(@PathVariable String serial) {
        Map<String, Object> body = new LinkedHashMap<>();
        try {
            List<DetectedIssue> issues = diagnosticService.diagnose(serial);
            body.put("status", "success");
            body.put("serial", serial);
            body.put("issue_count", issues.size());
            body.put("issues", issues);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            body.put("status", "error");
            body.put("message", e.getMessage());
            return ResponseEntity.status(500).body(body);
        }
    }
}
