package com.njiafix.service;

import com.fazecast.jSerialComm.SerialPort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ObdService {

    /**
     * Orodha ya "COM ports" zinazopatikana kwenye kompyuta hii - ikiwemo
     * ELM327 adapter YOYOTE iliyokwisha "pair"wa kwenye Bluetooth settings
     * za mfumo wa uendeshaji (Windows/Linux/Mac).
     */
    public List<String> listAvailablePorts() {
        List<String> ports = new ArrayList<>();
        for (SerialPort port : SerialPort.getCommPorts()) {
            ports.add(port.getSystemPortName() + " (" + port.getDescriptivePortName() + ")");
        }
        return ports;
    }

    /**
     * Inasoma Diagnostic Trouble Codes (DTC) zilizohifadhiwa kwenye ECU ya gari
     * kupitia amri za kiwango cha ELM327 (Mode 03).
     *
     * KUMBUKA: Hii inarudisha msimbo GHAFI (mfano "P0301") - tafsiri kamili ya maana
     * ya kila msimbo (mfano "Cylinder 1 Misfire") inahitaji hifadhidata kamili ya
     * OBD-II DTC ambayo bado hatujaiongeza.
     */
    public List<String> readDtcCodes(String portName) throws IOException, InterruptedException {
        SerialPort port = openPort(portName);
        try {
            sendCommand(port, "ATZ");
            Thread.sleep(1000);
            sendCommand(port, "ATE0");
            sendCommand(port, "ATSP0");
            String response = sendCommand(port, "03");
            return parseDtcResponse(response);
        } finally {
            port.closePort();
        }
    }

    /** Inafuta DTC zote na kuzima taa ya "Check Engine" (MIL) - Mode 04. */
    public void clearDtcCodes(String portName) throws IOException, InterruptedException {
        SerialPort port = openPort(portName);
        try {
            sendCommand(port, "ATZ");
            Thread.sleep(1000);
            sendCommand(port, "ATE0");
            sendCommand(port, "04");
        } finally {
            port.closePort();
        }
    }

    private SerialPort openPort(String portName) throws IOException {
        SerialPort port = SerialPort.getCommPort(portName);
        port.setBaudRate(38400);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 3000, 0);

        if (!port.openPort()) {
            throw new IOException("Imeshindikana kufungua port: " + portName
                    + " - hakikisha adapter imeoanishwa (paired) kwenye Bluetooth settings.");
        }
        return port;
    }

    private String sendCommand(SerialPort port, String command) throws IOException {
        OutputStream out = port.getOutputStream();
        InputStream in = port.getInputStream();

        out.write((command + "\r").getBytes());
        out.flush();

        StringBuilder response = new StringBuilder();
        byte[] buffer = new byte[1024];
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 3000) {
            if (in.available() > 0) {
                int bytesRead = in.read(buffer);
                response.append(new String(buffer, 0, bytesRead));
                if (response.toString().contains(">")) break; // ELM327 huisha response na ">"
            }
        }

        return response.toString();
    }

    private List<String> parseDtcResponse(String rawResponse) {
        List<String> codes = new ArrayList<>();
        String cleaned = rawResponse.replaceAll("[\\r\\n>]", "").trim();

        if (cleaned.isEmpty() || cleaned.equalsIgnoreCase("NO DATA") || cleaned.startsWith("43 00")) {
            return codes; // hakuna DTC iliyopatikana
        }

        codes.add("Raw ECU response: " + cleaned
                + " (tafsiri kamili ya msimbo inahitaji hifadhidata ya OBD-II - waona fundi kwa maelezo zaidi)");
        return codes;
    }
}
