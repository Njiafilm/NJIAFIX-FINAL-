package com.njiafix.service;

import com.njiafix.model.DetectedIssue;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Service
public class CameraDiagnosticService {

    /**
     * Ukaguzi wa jumla unaofanya kazi na camera yoyote ya IP (bila kujali brand),
     * kwa sababu itifaki maalum (ONVIF, RTSP variants) hutofautiana kwa kila kampuni.
     * Kadri utakavyojua brand maalum unazotumia zaidi, tunaweza kuongeza ukaguzi
     * wa kina zaidi (mfano: ONVIF GetDeviceInformation).
     */
    public List<DetectedIssue> diagnose(String ipAddress) {
        List<DetectedIssue> issues = new ArrayList<>();

        boolean httpOpen = isPortOpen(ipAddress, 80, 2000);   // Web UI ya camera nyingi
        boolean rtspOpen = isPortOpen(ipAddress, 554, 2000);  // RTSP video stream

        if (!httpOpen && !rtspOpen) {
            issues.add(new DetectedIssue("CAMERA_UNREACHABLE",
                    "Camera (" + ipAddress + ") haifikiki kwenye mtandao - angalia umeme, Wi-Fi/Ethernet, au IP address.",
                    "camera", "reboot_camera", "high"));
        } else if (!rtspOpen) {
            issues.add(new DetectedIssue("CAMERA_STREAM_DOWN",
                    "Camera (" + ipAddress + ") inaonekana kwenye mtandao lakini video stream (RTSP) haifanyi kazi.",
                    "camera", "reboot_camera", "medium"));
        }

        return issues;
    }

    private boolean isPortOpen(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
