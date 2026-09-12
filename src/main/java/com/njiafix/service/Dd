package com.njiafix.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.njiafix.model.DetectedIssue;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class PcDiagnosticService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Inauliza PcAgent inayoendesha kwenye kompyuta ya mteja (IP husika,
     * port 5556) na kutafsiri majibu yake kuwa DetectedIssue.
     */
    public List<DetectedIssue> diagnose(String ipAddress) {
        List<DetectedIssue> issues = new ArrayList<>();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ipAddress + ":5556/diagnose"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            issues.add(new DetectedIssue("PC_AGENT_UNREACHABLE",
                    "Haiwezi kufikia PC Agent kwenye " + ipAddress
                            + ":5556 - hakikisha PcAgent inaendesha kwenye kompyuta ya mteja "
                            + "na mko kwenye mtandao mmoja (Wi-Fi/LAN).",
                    "pc_remote", null, "high"));
            return issues;
        }

        try {
            JsonNode node = objectMapper.readTree(response.body());

            int cpuLoad = node.path("cpu_load_percent").asInt(-1);
            int ramFree = node.path("ram_free_percent").asInt(-1);
            int diskUsed = node.path("disk_used_percent").asInt(-1);

            if (cpuLoad >= 90) {
                issues.add(new DetectedIssue("PC_HIGH_CPU",
                        "CPU inatumika kwa " + cpuLoad + "% - kompyuta inaweza kuwa polepole sana.",
                        "pc_remote", null, "medium"));
            }
            if (ramFree >= 0 && ramFree < 10) {
                issues.add(new DetectedIssue("PC_LOW_RAM",
                        "RAM iliyobaki bure ni " + ramFree + "% tu.",
                        "pc_remote", null, "medium"));
            }
            if (diskUsed >= 90) {
                issues.add(new DetectedIssue("PC_LOW_DISK",
                        "Hifadhi (Disk) imejaa kwa " + diskUsed + "%.",
                        "pc_remote", null, "high"));
            }
        } catch (Exception e) {
            issues.add(new DetectedIssue("PC_AGENT_BAD_RESPONSE",
                    "PC Agent ilijibu lakini data haikueleweka: " + e.getMessage(),
                    "pc_remote", null, "medium"));
        }

        return issues;
    }
}
