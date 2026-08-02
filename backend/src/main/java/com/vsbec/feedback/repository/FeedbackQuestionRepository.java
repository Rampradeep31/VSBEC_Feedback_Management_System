package com.vsbec.feedback.repository;

import com.vsbec.feedback.entity.FeedbackQuestion;
import com.vsbec.feedback.entity.SubjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackQuestionRepository extends JpaRepository<FeedbackQuestion, Long> {
    List<FeedbackQuestion> findBySubjectTypeAndActiveTrueOrderByQuestionNumberAsc(SubjectType subjectType);
}
