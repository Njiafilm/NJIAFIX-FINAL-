package com.njiafix.repository;

import com.njiafix.model.RepairGuide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairGuideRepository extends JpaRepository<RepairGuide, Long> {

    List<RepairGuide> findByCategoryId(Long categoryId);

    // Sawa na: guides.filter(title__icontains=query) | guides.filter(symptom__icontains=query)
    List<RepairGuide> findByTitleContainingIgnoreCaseOrSymptomContainingIgnoreCase(String titleQuery, String symptomQuery);

    List<RepairGuide> findByCategoryIdAndTitleContainingIgnoreCaseOrCategoryIdAndSymptomContainingIgnoreCase(
            Long categoryId1, String titleQuery, Long categoryId2, String symptomQuery);

    // Inatumika na DiagnosticController kuunganisha tatizo lililogunduliwa na guide sahihi
    java.util.Optional<RepairGuide> findFirstByIssueCode(String issueCode);
}
