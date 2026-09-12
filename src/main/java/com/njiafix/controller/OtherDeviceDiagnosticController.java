package com.njiafix.controller;

import com.njiafix.model.DetectedIssue;
import com.njiafix.model.RepairGuide;
import com.njiafix.repository.RepairGuideRepository;
import com.njiafix.service.CameraDiagnosticService;
import com.njiafix.service.NetworkDeviceDiagnosticService;
import com.njiafix.service.ObdService;
import com.njiafix.service.PcDiagnosticService;
import com.njiafix.service.PrinterDiagnosticService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class OtherDeviceDiagnosticController {

    private final PrinterDiagnosticService printerDiagnosticService;
    private final CameraDiagnosticService cameraDiagnosticService;
    private final ObdService obdService;
    private final PcDiagnosticService pcDiagnosticService;
    private final NetworkDeviceDiagnosticService networkDeviceDiagnosticService;
    private final RepairGuideRepository guideRepository;

    public OtherDeviceDiagnosticController(PrinterDiagnosticService printerDiagnosticService,
                                            CameraDiagnosticService cameraDiagnosticService,
                                            ObdService obdService,
                                            PcDiagnosticService pcDiagnosticService,
                                            NetworkDeviceDiagnosticService networkDeviceDiagnosticService,
                                            RepairGuideRepository guideRepository) {
        this.printerDiagnosticService = printerDiagnosticService;
        this.cameraDiagnosticService = cameraDiagnosticService;
        this.obdService = obdService;
        this.pcDiagnosticService = pcDiagnosticService;
        this.networkDeviceDiagnosticService = networkDeviceDiagnosticService;
        this.guideRepository = guideRepository;
    }

    // --- PC YA MTEJA (kupitia PcAgent) ---

    @GetMapping("/diagnose/pc/{ip}")
    public ResponseEntity<Map<String, Object>> diagnosePc(@PathVariable String ip) {
        List<DetectedIssue> issues = pcDiagnosticService.diagnose(ip);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "success");
        body.put("target", ip);
        body.put("issues", enrichWithGuides(issues));
        return ResponseEntity.ok(body);
    }

    // --- ROUTER / UPS / SOLAR (ukaguzi wa jumla wa mtandao) ---

    @GetMapping("/diagnose/router/{ip}")
    public ResponseEntity<Map<String, Object>> diagnoseRouter(@PathVariable String ip) {
        return genericNetworkDiagnose(ip, "router", "Router");
    }

    @GetMapping("/diagnose/ups/{ip}")
    public ResponseEntity<Map<String, Object>> diagnoseUps(@PathVariable String ip) {
        return genericNetworkDiagnose(ip, "ups", "UPS/Inverter");
    }

    @GetMapping("/diagnose/solar/{ip}")
    public ResponseEntity<Map<String, Object>> diagnoseSolar(@PathVariable String ip) {
        return genericNetworkDiagnose(ip, "solar", "Solar System");
    }

    private ResponseEntity<Map<String, Object>> genericNetworkDiagnose(String ip, String category, String label) {
        List<DetectedIssue> issues = networkDeviceDiagnosticService.diagnose(ip, category, label);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "success");
        body.put("target", ip);
        body.put("issues", enrichWithGuides(issues));
        return ResponseEntity.ok(body);
    }

    // --- PRINTER ---

    @GetMapping("/diagnose/printer/local")
    public ResponseEntity<Map<String, Object>> diagnoseLocalPrinters() {
        List<Map<String, Object>> printers = printerDiagnosticService.diagnoseLocalPrinters();
        for (Map<String, Object> printer : printers) {
            @SuppressWarnings("unchecked")
            List<DetectedIssue> issues = (List<DetectedIssue>) printer.get("issues");
            printer.put("issues", enrichWithGuides(issues));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "success");
        body.put("printers", printers);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/diagnose/printer/network/{ip}")
    public ResponseEntity<Map<String, Object>> diagnoseNetworkPrinter(@PathVariable String ip) {
        List<DetectedIssue> issues = printerDiagnosticService.diagnoseNetworkPrinter(ip);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "success");
        body.put("target", ip);
        body.put("issues", enrichWithGuides(issues));
        return ResponseEntity.ok(body);
    }

    // --- CAMERA ---

    @GetMapping("/diagnose/camera/{ip}")
    public ResponseEntity<Map<String, Object>> diagnoseCamera(@PathVariable String ip) {
        List<DetectedIssue> issues = cameraDiagnosticService.diagnose(ip);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "success");
        body.put("target", ip);
        body.put("issues", enrichWithGuides(issues));
        return ResponseEntity.ok(body);
    }

    // --- GARI (OBD2) ---

    @GetMapping("/obd/ports")
    public ResponseEntity<Map<String, Object>> listObdPorts() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "success");
        body.put("ports", obdService.listAvailablePorts());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/diagnose/car/{portName}")
    public ResponseEntity<Map<String, Object>> diagnoseCar(@PathVariable String portName) {
        Map<String, Object> body = new LinkedHashMap<>();
        try {
            List<String> rawCodes = obdService.readDtcCodes(portName);
            List<DetectedIssue> issues = new ArrayList<>();

            if (rawCodes.isEmpty()) {
                body.put("status", "success");
                body.put("issues", List.of());
                body.put("message", "Hakuna DTC iliyopatikana - ECU haijaripoti tatizo lolote.");
                return ResponseEntity.ok(body);
            }

            for (String raw : rawCodes) {
                issues.add(new DetectedIssue("DTC_FOUND", raw, "car", "clear_dtc", "medium"));
            }

            body.put("status", "success");
            body.put("issues", enrichWithGuides(issues));
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            body.put("status", "error");
            body.put("message", e.getMessage());
            return ResponseEntity.status(500).body(body);
        }
    }

    @PostMapping("/obd/clear-dtc")
    public ResponseEntity<Map<String, Object>> clearDtc(@RequestBody Map<String, String> payload) {
        Map<String, Object> body = new LinkedHashMap<>();
        try {
            obdService.clearDtcCodes(payload.get("port"));
            body.put("status", "success");
            body.put("message", "DTC codes zimefutwa - taa ya Check Engine inapaswa kuzimika.");
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            body.put("status", "error");
            body.put("message", e.getMessage());
            return ResponseEntity.status(500).body(body);
        }
    }

    // --- Msaada: unganisha kila tatizo na guide yake kutoka database ---
    private List<Map<String, Object>> enrichWithGuides(List<DetectedIssue> issues) {
        List<Map<String, Object>> enriched = new ArrayList<>();
        for (DetectedIssue issue : issues) {
            Map<String, Object> issueMap = new LinkedHashMap<>();
            issueMap.put("code", issue.code);
            issueMap.put("description", issue.description);
            issueMap.put("category", issue.category);
            issueMap.put("action", issue.action);
            issueMap.put("severity", issue.severity);

            guideRepository.findFirstByIssueCode(issue.code).ifPresentOrElse(
                    guide -> issueMap.put("guide", toGuideMap(guide)),
                    () -> issueMap.put("guide", null)
            );
            enriched.add(issueMap);
        }
        return enriched;
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
