package com.njiafix.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/")
public class RepairGuideController {

    // Hifadhi ya Kina ya Miongozo na Kategoria za Kudumu (Permanent Repair Guides)
    private static final Map<String, RepairGuide> REPAIR_GUIDES = new LinkedHashMap<>();

    static {
        REPAIR_GUIDES.put("phone", new RepairGuide(
                "Ukarabati wa Simu na Programu (Mobile Maintenance)",
                "Simu na Vifaa vya Mkononi",
                List.of(
                        "1. Angalia afya ya betri na kiwango cha chaji kupitia ADB au multimeter.",
                        "2. Safisha cache na faili taka zinazojaza kumbukumbu ya ndani (Storage).",
                        "3. Hakikisha mfumo wa uendeshaji (OS) umesasishwa ili kuzuia mdudu wa kiufundi."
                )));

        REPAIR_GUIDES.put("bootloader", new RepairGuide(
                "Kufungua na Kutatua Bootloader (Bootloader Unlocking & Fixes)",
                "Simu na Vifaa vya Mkononi",
                List.of(
                        "1. Washa 'OEM Unlocking' na 'USB Debugging' kupitia Developer Options kwenye simu.",
                        "2. Unganisha simu kwenye PC na uwashe mode ya fastboot kwa amri: 'adb reboot bootloader'.",
                        "3. Hakikisha madereva (USB Drivers) ya simu yamesakinishwa vizuri kwenye PC kabla ya kutuma amri ya kufungua."
                )));

        REPAIR_GUIDES.put("recovery", new RepairGuide(
                "Matumizi ya Recovery Mode na Kuweka Upya (Recovery Mode & Flashing)",
                "Simu na Vifaa vya Mkononi",
                List.of(
                        "1. Zima simu kabisa, kisha bonyeza kitufe cha kuwasha na sauti chini (Power + Volume Down) kwa wakati mmoja.",
                        "2. Chagua 'Wipe Data/Factory Reset' iwapo simu imefungwa au ina-hang kwenye logo.",
                        "3. Tumia 'Apply update from ADB' au maalum custom recovery kusakinisha mfumo mpya."
                )));

        REPAIR_GUIDES.put("pc", new RepairGuide(
                "Ukarabati na Usalama wa Kompyuta (PC Health & Cleanup)",
                "Kompyuta na Laptops",
                List.of(
                        "1. Kagua nafasi iliyobaki kwenye diski kuu (Disk Space Check) na uendeshe Disk Cleanup.",
                        "2. Simamisha programu zisizohitajika zinazotumia rasilimali nyingi wakati wa kuwasha (Startup Optimization).",
                        "3. Endesha uchunguzi wa mfumo (SFC / DISM scan) kurekebisha faili zilizoharibika za Windows."
                )));

        REPAIR_GUIDES.put("printer", new RepairGuide(
                "Utatuzi wa Printa na Hitilafu za Wino (Printer Troubleshooting)",
                "Vifaa vya Ofisi na Printa",
                List.of(
                        "1. Angalia kama kuna karatasi zilizokwama (Paper Jam) kwenye sehemu ya kuvutia au kutoa karatasi.",
                        "2. Endesha kipengele cha kusafisha vichwa vya wino (Print Head Cleaning) kupitia kompyuta kama maandishi yanatoka hafifu au mistari.",
                        "3. Hakikisha miunganisho ya mtandao (Wi-Fi) au waya wa USB iko sawa na printa ipo kwenye 'Online' mode."
                )));

        REPAIR_GUIDES.put("photocopy", new RepairGuide(
                "Matengenezo na Utatuzi wa Mashine za Photocopy",
                "Vifaa vya Ofisi na Printa",
                List.of(
                        "1. Safisha kioo cha scanner na vioo vya ndani kwa kitambaa kisicho na unyevu ili kuondoa madoa na michirizi kwenye nakala.",
                        "2. Angalia hali ya wino wa unga (Toner) na usafishe eneo la kupasha joto karatasi (Fuser Unit) kuzuia karatasi kukunjamana.",
                        "3. Kagua roli za kuvutia karatasi (Pickup Rollers); kama zimechakaa au zina vumbi, zisafishe au uzibadilishe."
                )));

        REPAIR_GUIDES.put("camera", new RepairGuide(
                "Utatuzi wa Kamera za Kidijitali na CCTV (Camera Maintenance)",
                "Kamera na Usalama",
                List.of(
                        "1. Safisha lenzi ya kamera kwa kitambaa maalum cha microfiber na kimiminika cha kusafishia lenzi.",
                        "2. Hakikisha kadi ya kumbukumbu (SD Card) imesafishwa au kuumbizwa upya (Formatted) kwenye kamera ili kuepusha makosa ya kusoma faili.",
                        "3. Kagua miunganisho ya nyaya za umeme na mtandao (BNC / Ethernet cables) kwa kamera za usalama (CCTV) zinapokosa kuonekana."
                )));

        REPAIR_GUIDES.put("car", new RepairGuide(
                "Utatuzi wa Awali wa Magari (OBD-II Diagnostics)",
                "Magari na Vyombo vya Usafiri",
                List.of(
                        "1. Unganisha kifaa cha uchunguzi cha OBD-II kwenye bandari ya kompyuta ya gari (Dashboard port).",
                        "2. Soma namba za makosa (Diagnostic Trouble Codes - DTC) ili kubaini eneo lenye tatizo kwenye injini au sensa.",
                        "3. Futa makosa ya muda baada ya kufanya marekebisho ya mitambo au kubadilisha sehemu iliyoharibika."
                )));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "success");
        body.put("message", "Karibu kwenye NjiaFix Diagnostic Engine API");
        body.put("categories", distinctCategories());
        body.put("modules", new ArrayList<>(REPAIR_GUIDES.keySet()));
        return ResponseEntity.ok(body);
    }

    @GetMapping("/api/categories")
    public ResponseEntity<Map<String, Object>> getCategories() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "success");
        body.put("categories", distinctCategories());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/api/repair-guides/{deviceType}")
    public ResponseEntity<Map<String, Object>> getRepairGuide(@PathVariable String deviceType) {
        RepairGuide guide = REPAIR_GUIDES.get(deviceType.toLowerCase());
        Map<String, Object> body = new LinkedHashMap<>();
        if (guide != null) {
            body.put("status", "success");
            body.put("data", guide);
            return ResponseEntity.ok(body);
        } else {
            body.put("status", "error");
            body.put("message", "Mwongozo haupatikani kwa kifaa hiki au haijasajiliwa kwenye mfumo.");
            return ResponseEntity.status(404).body(body);
        }
    }

    @GetMapping("/api/search")
    public ResponseEntity<Map<String, Object>> searchGuides(@RequestParam(name = "q", defaultValue = "") String q) {
        String query = q.toLowerCase().trim();
        Map<String, RepairGuide> results = new LinkedHashMap<>();

        for (Map.Entry<String, RepairGuide> entry : REPAIR_GUIDES.entrySet()) {
            String key = entry.getKey();
            RepairGuide g = entry.getValue();
            boolean matches = key.contains(query)
                    || g.title.toLowerCase().contains(query)
                    || g.category.toLowerCase().contains(query)
                    || g.steps.stream().anyMatch(s -> s.toLowerCase().contains(query));
            if (matches) {
                results.put(key, g);
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "success");
        body.put("query", query);
        body.put("results", results.isEmpty() ? REPAIR_GUIDES : results);
        return ResponseEntity.ok(body);
    }

    private List<String> distinctCategories() {
        List<String> categories = new ArrayList<>();
        for (RepairGuide g : REPAIR_GUIDES.values()) {
            if (!categories.contains(g.category)) {
                categories.add(g.category);
            }
        }
        return categories;
    }

    // --- Model ---
    static class RepairGuide {
        public String title;
        public String category;
        public List<String> steps;

        RepairGuide(String title, String category, List<String> steps) {
            this.title = title;
            this.category = category;
            this.steps = steps;
        }
    }
}
