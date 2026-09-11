package com.njiafix.controller;

import com.njiafix.model.DetectedIssue;
import com.njiafix.model.RepairGuide;
import com.njiafix.repository.RepairGuideRepository;
import com.njiafix.service.DeviceDetectionService;
import com.njiafix.service.DiagnosticService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class DiagnosticController {

    private final DeviceDetectionService deviceDetectionService;
    private final DiagnosticService diagnosticService;
    private final RepairGuideRepository guideRepository;

    public DiagnosticController(DeviceDetectionService deviceDetectionService,
                                 DiagnosticService diagnosticService,
                                 RepairGuideRepository guideRepository) {
        this.deviceDetectionService = deviceDetectionService;
        this.diagnosticService = diagnosticService;
        this.guideRepository = guideRepository;
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

    // Uchunguzi wa kifaa maalum - inarejesha matatizo YALIYOGUNDULIWA KIOTOMATIKI
    // pamoja na guide inayolingana na kila moja (fundi hatafuti mwenyewe)
    @GetMapping("/api/diagnose/{serial}")
    public ResponseEntity<Map<String, Object>> diagnose(@PathVariable String serial) {
        Map<String, Object> body = new LinkedHashMap<>();
        try {
            List<DetectedIssue> issues = diagnosticService.diagnose(serial);
            List<Map<String, Object>> enrichedIssues = new ArrayList<>();

            for (DetectedIssue issue : issues) {
                Map<String, Object> issueMap = new LinkedHashMap<>();
                issueMap.put("code", issue.code);
                issueMap.put("description", issue.description);
                issueMap.put("category", issue.category);
                issueMap.put("action", issue.action); // null = hakuna amri ya kiotomatiki
                issueMap.put("severity", issue.severity);

                // Tafuta guide inayolingana na tatizo hili kwenye database
                guideRepository.findFirstByIssueCode(issue.code).ifPresentOrElse(
                        guide -> issueMap.put("guide", toGuideMap(guide)),
                        () -> issueMap.put("guide", null)
                );

                enrichedIssues.add(issueMap);
            }

            body.put("status", "success");
            body.put("serial", serial);
            body.put("issue_count", enrichedIssues.size());
            body.put("issues", enrichedIssues);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            body.put("status", "error");
            body.put("message", e.getMessage());
            return ResponseEntity.status(500).body(body);
        }
    }

    private Map<String, Object> toGuideMap(RepairGuide g) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", g.getTitle());
        m.put("symptom", g.getSymptom());
        m.put("solution_steps", g.getSolutionSteps());
        m.put("difficulty", g.getDifficulty().label);
        return m;
    }
}
