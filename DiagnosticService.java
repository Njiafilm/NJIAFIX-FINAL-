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
     * Vigezo hivi ni mwanzo tu (starting logic) - unaweza kuongeza vigezo zaidi
     * kadri unavyojifunza matatizo ya kawaida ya wateja wako.
     */
    public List<DetectedIssue> diagnose(String serial) throws Exception {
        List<DetectedIssue> issues = new ArrayList<>();

        // 1. Angalia betri
        String batteryDump = deviceDetectionService.runShell(serial, "dumpsys", "battery");
        Integer batteryLevel = extractInt(batteryDump, "level:\\s*(\\d+)");
        if (batteryLevel != null && batteryLevel < 20) {
            issues.add(new DetectedIssue(
                    "LOW_BATTERY",
                    "Kiwango cha betri ni chini ya 20% (" + batteryLevel + "%). Chaji kifaa au angalia afya ya betri.",
                    "simu", "reboot", "medium"
            ));
        }

        // 2. Angalia nafasi ya hifadhi (storage) - /data partition
        String storageDump = deviceDetectionService.runShell(serial, "df", "/data");
        Integer usagePercent = extractInt(storageDump, "(\\d+)%");
        if (usagePercent != null && usagePercent >= 90) {
            issues.add(new DetectedIssue(
                    "STORAGE_FULL",
                    "Hifadhi ya ndani imejaa kwa " + usagePercent + "%. Cache inahitaji kusafishwa.",
                    "simu", "clear_cache", "high"
            ));
        }

        // 3. Angalia kama kifaa hakijibu vizuri (boot completed)
        String bootStatus = deviceDetectionService.runShell(serial, "getprop", "sys.boot_completed").trim();
        if (!bootStatus.equals("1")) {
            issues.add(new DetectedIssue(
                    "BOOT_INCOMPLETE",
                    "Mfumo haujawasha kikamilifu au una hitilafu ya boot. Inashauriwa kuanzisha upya (reboot).",
                    "simu", "reboot", "high"
            ));
        }

        return issues;
    }

    private Integer extractInt(String text, String regexWithOneGroup) {
        Pattern p = Pattern.compile(regexWithOneGroup);
        Matcher m = p.matcher(text);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}
