package com.njiafix.controller;

import com.njiafix.model.DeviceCategory;
import com.njiafix.model.RepairGuide;
import com.njiafix.repository.DeviceCategoryRepository;
import com.njiafix.repository.RepairGuideRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RepairController {

    private final DeviceCategoryRepository categoryRepository;
    private final RepairGuideRepository guideRepository;

    public RepairController(DeviceCategoryRepository categoryRepository,
                             RepairGuideRepository guideRepository) {
        this.categoryRepository = categoryRepository;
        this.guideRepository = guideRepository;
    }

    // Sawa na api_categories()
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories() {
        List<DeviceCategory> cats = categoryRepository.findAll();
        List<Map<String, Object>> data = cats.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("icon", c.getIconName());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("categories", data);
        return ResponseEntity.ok(body);
    }

    // Sawa na api_guides() - inasaidia ?category_id= na ?q=
    @GetMapping("/guides")
    public ResponseEntity<Map<String, Object>> getGuides(
            @RequestParam(required = false) Long category_id,
            @RequestParam(required = false, defaultValue = "") String q) {

        List<RepairGuide> guides;

        if (category_id != null && !q.isBlank()) {
            guides = guideRepository.findByCategoryIdAndTitleContainingIgnoreCaseOrCategoryIdAndSymptomContainingIgnoreCase(
                    category_id, q, category_id, q);
        } else if (category_id != null) {
            guides = guideRepository.findByCategoryId(category_id);
        } else if (!q.isBlank()) {
            guides = guideRepository.findByTitleContainingIgnoreCaseOrSymptomContainingIgnoreCase(q, q);
        } else {
            guides = guideRepository.findAll();
        }

        List<Map<String, Object>> data = guides.stream().limit(100).map(g -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", g.getId());
            m.put("title", g.getTitle());
            m.put("category_name", g.getCategory() != null ? g.getCategory().getName() : null);
            m.put("brand", g.getBrand());
            m.put("symptom", g.getSymptom());
            m.put("solution_steps", g.getSolutionSteps());
            m.put("difficulty", g.getDifficulty().label);
            m.put("image", g.getImagePath());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("guides", data);
        return ResponseEntity.ok(body);
    }

    // Sawa na ongeza_repair_guide() (bila upload ya picha kwa sasa)
    @PostMapping("/guides")
    public ResponseEntity<Map<String, Object>> addGuide(@RequestBody Map<String, Object> payload) {
        Map<String, Object> body = new LinkedHashMap<>();

        Long categoryId = payload.get("category") != null
                ? Long.valueOf(payload.get("category").toString()) : null;

        DeviceCategory category = categoryId != null
                ? categoryRepository.findById(categoryId).orElse(null) : null;

        RepairGuide guide = new RepairGuide();
        guide.setCategory(category);
        guide.setTitle((String) payload.getOrDefault("title", ""));
        guide.setBrand((String) payload.getOrDefault("brand", ""));
        guide.setSymptom((String) payload.getOrDefault("symptom", ""));
        guide.setSolutionSteps((String) payload.getOrDefault("solution_steps", ""));

        String difficultyStr = (String) payload.getOrDefault("difficulty", "easy");
        try {
            guide.setDifficulty(RepairGuide.Difficulty.valueOf(difficultyStr));
        } catch (IllegalArgumentException e) {
            guide.setDifficulty(RepairGuide.Difficulty.easy);
        }

        RepairGuide saved = guideRepository.save(guide);

        body.put("status", "success");
        body.put("id", saved.getId());
        return ResponseEntity.ok(body);
    }
}
