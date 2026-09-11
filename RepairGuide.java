package com.njiafix.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "repair_guide")
public class RepairGuide {

    public enum Difficulty {
        easy("Rahisi"),
        medium("Wastani"),
        hard("Ngumu - Fundi Anahitajika");

        public final String label;

        Difficulty(String label) {
            this.label = label;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private DeviceCategory category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 100)
    private String brand = "";

    @Lob
    private String symptom;

    @Lob
    @Column(name = "solution_steps")
    private String solutionSteps;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Difficulty difficulty = Difficulty.easy;

    @Column(name = "image_path")
    private String imagePath; // njia ya faili ya picha (badala ya Django ImageField)

    // Inaunganisha guide hii na tatizo maalum linalogunduliwa kiotomatiki
    // na DiagnosticService (mfano: "LOW_BATTERY", "STORAGE_FULL"). Null = guide ya jumla.
    @Column(name = "issue_code", length = 50)
    private String issueCode;

    private Integer views = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public RepairGuide() {
    }

    // --- Getters / Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DeviceCategory getCategory() {
        return category;
    }

    public void setCategory(DeviceCategory category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSymptom() {
        return symptom;
    }

    public void setSymptom(String symptom) {
        this.symptom = symptom;
    }

    public String getSolutionSteps() {
        return solutionSteps;
    }

    public void setSolutionSteps(String solutionSteps) {
        this.solutionSteps = solutionSteps;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public String getIssueCode() {
        return issueCode;
    }

    public void setIssueCode(String issueCode) {
        this.issueCode = issueCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
