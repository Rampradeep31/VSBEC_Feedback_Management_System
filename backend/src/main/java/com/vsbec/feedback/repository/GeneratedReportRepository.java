package com.vsbec.feedback.repository;

import com.vsbec.feedback.entity.GeneratedReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, Long> {
    List<GeneratedReport> findByClassGroup_IdOrderByGeneratedAtDesc(Long classId);
}
