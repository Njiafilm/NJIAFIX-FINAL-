package com.njiafix.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeviceDetectionService {

    /**
     * Inaendesha "adb devices" na kurejesha orodha ya serial numbers
     * za vifaa vilivyounganishwa na vinavyotambulika (state = device).
     */
    public List<String> listConnectedDevices() throws Exception {
        List<String> devices = new ArrayList<>();

        ProcessBuilder pb = new ProcessBuilder("adb", "devices");
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // ruka header: "List of devices attached"
                    continue;
                }
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                if (parts.length == 2 && parts[1].equals("device")) {
                    devices.add(parts[0]); // serial number
                }
            }
        }

        process.waitFor();
        return devices;
    }

    /** Inaendesha amri ya "adb -s <serial> shell <command>" na kurejesha output. */
    public String runShell(String serial, String... shellCommand) throws Exception {
        List<String> fullCmd = new ArrayList<>();
        fullCmd.add("adb");
        fullCmd.add("-s");
        fullCmd.add(serial);
        fullCmd.add("shell");
        for (String part : shellCommand) fullCmd.add(part);

        ProcessBuilder pb = new ProcessBuilder(fullCmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }
        process.waitFor();
        return output.toString();
    }
}
