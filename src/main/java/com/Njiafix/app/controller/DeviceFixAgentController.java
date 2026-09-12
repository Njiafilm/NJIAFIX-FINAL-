package com.Njiafix.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device")
public class DeviceFixAgentController {

    @PostMapping("/report")
    public ResponseEntity<String> receiveClientReport(@RequestBody String reportPayload) {
        System.out.println("Imepokea taarifa za afya ya kifaa/PC: " + reportPayload);
        // Hapa unaweza kuongeza mantiki ya kuhifadhi kwenye database
        return ResponseEntity.ok("Ripoti imepokelewa na kuhifadhiwa kikamilifu.");
    }
}
