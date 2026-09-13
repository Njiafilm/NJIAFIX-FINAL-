package com.njiafix.service;

import com.njiafix.model.DetectedIssue;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import javax.print.attribute.standard.PrinterState;
import javax.print.attribute.standard.PrinterStateReason;
import javax.print.attribute.standard.PrinterStateReasons;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrinterDiagnosticService {

    // --- Printer/Photocopy za USB (zilizosakinishwa kwenye kompyuta hii) ---
    public List<Map<String, Object>> diagnoseLocalPrinters() {
        List<Map<String, Object>> results = new ArrayList<>();
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);

        for (PrintService service : services) {
            List<DetectedIssue> issues = new ArrayList<>();
            String name = service.getName();

            PrinterState state = service.getAttribute(PrinterState.class);
            PrinterStateReasons reasons = service.getAttribute(PrinterStateReasons.class);
            PrinterIsAcceptingJobs accepting = service.getAttribute(PrinterIsAcceptingJobs.class);

            if (state == PrinterState.STOPPED) {
                issues.add(new DetectedIssue("PRINTER_STOPPED",
                        "Printa '" + name + "' imesimama (stopped) - haichapishi kazi yoyote.",
                        "printer", "restart_spooler", "high"));
            }

            if (accepting == PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS) {
                issues.add(new DetectedIssue("PRINTER_NOT_ACCEPTING",
                        "Printa '" + name + "' haikubali kazi mpya za kuchapisha.",
                        "printer", "restart_spooler", "high"));
            }

            if (reasons != null) {
                for (Object reasonObj : reasons.keySet()) {
                    String reason = ((PrinterStateReason) reasonObj).toString().toLowerCase();
                    if (reason.contains("media-jam")) {
                        issues.add(new DetectedIssue("PRINTER_PAPER_JAM",
                                "Printa '" + name + "' ina karatasi iliyokwama.",
                                "printer", null, "high"));
                    } else if (reason.contains("toner-low") || reason.contains("marker-supply-low")) {
                        issues.add(new DetectedIssue("PRINTER_LOW_TONER",
                                "Printa '" + name + "' ina wino/toner kidogo.",
                                "printer", null, "medium"));
                    } else if (reason.contains("door-open") || reason.contains("cover-open")) {
                        issues.add(new DetectedIssue("PRINTER_COVER_OPEN",
                                "Mlango/kifuniko cha printa '" + name + "' kiko wazi.",
                                "printer", null, "medium"));
                    }
                }
            }

            Map<String, Object> printerResult = new LinkedHashMap<>();
            printerResult.put("name", name);
            printerResult.put("issues", issues);
            results.add(printerResult);
        }

        return results;
    }

    // --- Printer za Mtandao (Wi-Fi/Ethernet - zina IP address) ---
    public List<DetectedIssue> diagnoseNetworkPrinter(String ipAddress) {
        List<DetectedIssue> issues = new ArrayList<>();

        boolean rawPortOpen = isPortOpen(ipAddress, 9100, 2000); // JetDirect/Raw printing
        boolean ippPortOpen = isPortOpen(ipAddress, 631, 2000);  // IPP

        if (!rawPortOpen && !ippPortOpen) {
            issues.add(new DetectedIssue("NETWORK_PRINTER_UNREACHABLE",
                    "Printa ya mtandao (" + ipAddress + ") haifikiki - angalia umeme, Wi-Fi/Ethernet, au IP address.",
                    "printer", null, "high"));
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
