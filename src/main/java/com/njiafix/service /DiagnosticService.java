package com.njiafix.service;

import com.njiafix.model.DetectedIssue;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DiagnosticService {

    private final DeviceDetectionService deviceDetectionService;

    public DiagnosticService(DeviceDetectionService deviceDetectionService) {
        this.deviceDetectionService = deviceDetectionService;
    }

    /**
     * Inachunguza kifaa (serial) na kurejesha orodha ya matatizo yaliyogunduliwa.
     * Kila "code" hapa lazima ilingane na issue_code kwenye jedwali la repair_guide
     * (data.sql) ili DiagnosticController iweze kuunganisha tatizo na guide sahihi.
     */
    public List<DetectedIssue> diagnose(String serial) throws Exception {
        List<DetectedIssue> issues = new ArrayList<>();

        checkBattery(serial, issues);
        checkStorage(serial, issues);
        checkBootStatus(serial, issues);
        checkRam(serial, issues);
        checkNetwork(serial, issues);
        checkUptime(serial, issues);

        return issues;
    }

    private void checkBattery(String serial, List<DetectedIssue> issues) throws Exception {
        String batteryDump = deviceDetectionService.runShell(serial, "dumpsys", "battery");
        Integer batteryLevel = extractInt(batteryDump, "level:\\s*(\\d+)");
        if (batteryLevel != null && batteryLevel < 20) {
            issues.add(new DetectedIssue(
                    "LOW_BATTERY",
                    "Kiwango cha betri ni chini ya 20% (" + batteryLevel + "%). Chaji kifaa au angalia afya ya betri.",
                    "simu", null, "medium" // hakuna amri ya kiotomatiki - fundi anafuata hatua za guide
            ));
        }
    }

    private void checkStorage(String serial, List<DetectedIssue> issues) throws Exception {
        String storageDump = deviceDetectionService.runShell(serial, "df", "/data");
        Integer usagePercent = extractInt(storageDump, "(\\d+)%");
        if (usagePercent != null && usagePercent >= 90) {
            issues.add(new DetectedIssue(
                    "STORAGE_FULL",
                    "Hifadhi ya ndani imejaa kwa " + usagePercent + "%. Cache inahitaji kusafishwa.",
                    "simu", "clear_cache", "high"
            ));
        }
    }

    private void checkBootStatus(String serial, List<DetectedIssue> issues) throws Exception {
        String bootStatus = deviceDetectionService.runShell(serial, "getprop", "sys.boot_completed").trim();
        if (!bootStatus.equals("1")) {
            issues.add(new DetectedIssue(
                    "BOOT_INCOMPLETE",
                    "Mfumo haujawasha kikamilifu au una hitilafu ya boot. Inashauriwa kuanzisha upya (reboot).",
                    "simu", "reboot", "high"
            ));
        }
    }

    private void checkRam(String serial, List<DetectedIssue> issues) throws Exception {
        // "adb shell dumpsys meminfo" inatoa muhtasari wa RAM chini ya "Total RAM:" na "Free RAM:"
        String memDump = deviceDetectionService.runShell(serial, "dumpsys", "meminfo");
        Long totalKb = extractLong(memDump, "Total RAM:\\s*([\\d,]+)K");
        Long freeKb = extractLong(memDump, "Free RAM:\\s*([\\d,]+)K");

        if (totalKb != null && freeKb != null && totalKb > 0) {
            int freePercent = (int) ((freeKb * 100) / totalKb);
            if (freePercent < 10) {
                issues.add(new DetectedIssue(
                        "LOW_RAM",
                        "Kumbukumbu ya RAM iliyobaki ni chini ya 10% (" + freePercent + "%). Programu nyingi zinaendesha nyuma.",
                        "simu", "reboot", "medium"
                ));
            }
        }
    }

    private void checkNetwork(String serial, List<DetectedIssue> issues) throws Exception {
        // Jaribu ping fupi kutoka kwenye kifaa chenyewe kwenda 8.8.8.8
        String pingResult = deviceDetectionService.runShell(serial, "ping", "-c", "1", "-W", "2", "8.8.8.8");
        boolean hasInternet = pingResult.contains("1 packets transmitted, 1") || pingResult.contains("1 received");
        if (!hasInternet) {
            issues.add(new DetectedIssue(
                    "NO_INTERNET",
                    "Kifaa hakina muunganisho wa intaneti unaofanya kazi (Wi-Fi/Data).",
                    "simu", "toggle_wifi", "medium"
            ));
        }
    }

    private void checkUptime(String serial, List<DetectedIssue> issues) throws Exception {
        String uptimeOutput = deviceDetectionService.runShell(serial, "uptime");
        // Tafuta idadi ya siku kwenye output, mfano: "up 5 days, 3:12"
        Integer days = extractInt(uptimeOutput, "up\\s+(\\d+)\\s+day");
        if (days != null && days >= 3) {
            issues.add(new DetectedIssue(
                    "UPTIME_HIGH",
                    "Kifaa hakijawahi kuzimwa kwa siku " + days + ". Reboot inashauriwa kusafisha kumbukumbu ya mfumo.",
                    "simu", "reboot", "low"
            ));
        }
    }

    private Integer extractInt(String text, String regexWithOneGroup) {
        Matcher m = Pattern.compile(regexWithOneGroup).matcher(text);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Long extractLong(String text, String regexWithOneGroup) {
        Matcher m = Pattern.compile(regexWithOneGroup).matcher(text);
        if (m.find()) {
            try {
                return Long.parseLong(m.group(1).replace(",", ""));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}
