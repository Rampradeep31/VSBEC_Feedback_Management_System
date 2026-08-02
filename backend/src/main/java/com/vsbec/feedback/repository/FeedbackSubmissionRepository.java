package com.vsbec.feedback.repository;

import com.vsbec.feedback.entity.FeedbackSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackSubmissionRepository extends JpaRepository<FeedbackSubmission, Long> {
    boolean existsByStudent_IdAndSubject_Id(Long studentId, Long subjectId);
    List<FeedbackSubmission> findByStudent_Id(Long studentId);
    long countBySubject_Id(Long subjectId);

    // Distinct students (by id) who have submitted at least one subject in a class - for dashboard "submitted" count
    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(DISTINCT fs.student.id) FROM FeedbackSubmission fs WHERE fs.subject.classGroup.id = :classId")
    long countDistinctStudentsSubmittedForClass(Long classId);
}
