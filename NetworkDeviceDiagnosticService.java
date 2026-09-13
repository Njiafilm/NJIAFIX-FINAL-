package com.njiafix.service;

import com.njiafix.model.DetectedIssue;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Service
public class NetworkDeviceDiagnosticService {

    /**
     * Ukaguzi wa jumla wa "je kifaa kiko hai kwenye mtandao" - kwa Router,
     * UPS/Inverter, na Solar system zenye network interface. Vifaa hivi
     * vina itifaki tofauti kabisa kwa kila brand (Modbus, SNMP, vendor APIs),
     * kwa hiyo hii ni ukaguzi wa awali tu (reachability). Ukishatupa jina la
     * brand/model maalum unazotumia zaidi, tunaweza kuongeza usomaji wa kina
     * zaidi (mfano voltage, asilimia ya betri, signal strength).
     */
    public List<DetectedIssue> diagnose(String ipAddress, String category, String categoryLabel) {
        List<DetectedIssue> issues = new ArrayList<>();

        boolean httpOpen = isPortOpen(ipAddress, 80, 2000) || isPortOpen(ipAddress, 443, 2000);

        if (!httpOpen) {
            issues.add(new DetectedIssue(
                    category.toUpperCase() + "_UNREACHABLE",
                    categoryLabel + " (" + ipAddress + ") haifikiki kwenye mtandao.",
                    category, null, "high"));
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
