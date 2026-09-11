package com.njiafix.model;

public class DetectedIssue {
    public String code;          // mfano: "LOW_BATTERY", "STORAGE_FULL"
    public String description;   // maelezo kwa lugha ya fundi/mteja
    public String category;      // "simu", "kompyuta", "decoder" - inatumika na /execute-fix
    public String action;        // "reboot", "clear_cache" - inatumika na /execute-fix
    public String severity;      // "low", "medium", "high"

    public DetectedIssue(String code, String description, String category, String action, String severity) {
        this.code = code;
        this.description = description;
        this.category = category;
        this.action = action;
        this.severity = severity;
    }
}
