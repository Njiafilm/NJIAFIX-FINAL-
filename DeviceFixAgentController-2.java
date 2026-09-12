package com.njiafix.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*") // sawa na flask_cors CORS(app) - punguza hii kwenye production
public class DeviceFixAgentController {

    @PostMapping("/execute-fix")
    public ResponseEntity<Map<String, Object>> executeFix(@RequestBody Map<String, String> data) {
        String deviceCategory = data.get("category"); // simu, kompyuta, printer, camera, decoder
        String action = data.get("action");
        String target = data.get("target"); // IP address (camera/network printer) - hiari

        Map<String, Object> body = new LinkedHashMap<>();
        List<String> cmd = null;

        try {
            // 1. Marekebisho ya Simu (Android kupitia ADB)
            if ("simu".equals(deviceCategory)) {
                if ("reboot".equals(action)) {
                    cmd = List.of("adb", "reboot");
                } else if ("clear_cache".equals(action)) {
                    cmd = List.of("adb", "shell", "pm", "trim-caches", "999G");
                } else if ("toggle_wifi".equals(action)) {
                    new ProcessBuilder("adb", "shell", "svc", "wifi", "disable").start().waitFor();
                    Thread.sleep(1500);
                    cmd = List.of("adb", "shell", "svc", "wifi", "enable");
                }

                // 2. Marekebisho ya Kompyuta (Windows network/system commands)
            } else if ("kompyuta".equals(deviceCategory)) {
                if ("flush_dns".equals(action)) {
                    cmd = List.of("ipconfig", "/flushdns");
                } else if ("reset_network".equals(action)) {
                    cmd = List.of("netsh", "winsock", "reset");
                }

                // 3. Marekebisho ya Printer/Photocopy
            } else if ("printer".equals(deviceCategory)) {
                if ("restart_spooler".equals(action)) {
                    // Windows Print Spooler restart - inahitaji ruhusa za admin
                    new ProcessBuilder("cmd", "/c", "net", "stop", "spooler").start().waitFor();
                    Thread.sleep(1000);
                    cmd = List.of("cmd", "/c", "net", "start", "spooler");
                }

                // 4. Marekebisho ya Camera (kupitia HTTP - inahitaji "target" IP)
            } else if ("camera".equals(deviceCategory)) {
                if ("reboot_camera".equals(action) && target != null) {
                    String result = sendHttpReboot(target);
                    body.put("status", "success");
                    body.put("message", "Amri ya reboot imetumwa kwa camera " + target);
                    body.put("output", result);
                    return ResponseEntity.ok(body);
                }

                // 5. Ving'amuzi / Sat (Kupitia Serial/COM ports au IP commands)
            } else if ("decoder".equals(deviceCategory)) {
                if ("reboot_sat".equals(action)) {
                    // Amri maalum ya serial port au telnet kwenda kwenye decoder
                    // TODO: ongeza logic halisi hapa ukishathibitisha model ya decoder
                }
            }

            if (cmd == null) {
                body.put("status", "error");
                body.put("message", "Amri haijatambuliwa kwa kifaa hiki.");
                return ResponseEntity.badRequest().body(body);
            }

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

    private String sendHttpReboot(String ipAddress) {
        try {
            URL url = new URL("http://" + ipAddress + "/cgi-bin/hopper?action=reboot");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            return "HTTP response code: " + responseCode;
        } catch (Exception e) {
            return "Hitilafu: " + e.getMessage()
                    + " (baadhi ya camera zina njia tofauti ya reboot - zingatia CGI endpoint ya brand yako)";
        }
    }
}
