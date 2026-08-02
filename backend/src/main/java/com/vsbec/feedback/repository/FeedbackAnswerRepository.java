package com.vsbec.feedback.repository;

import com.vsbec.feedback.entity.FeedbackAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface FeedbackAnswerRepository extends JpaRepository<FeedbackAnswer, Long> {

    List<FeedbackAnswer> findBySubject_Id(Long subjectId);

    @Query("SELECT fa.question.questionNumber as qNum, AVG(fa.rating) as avgRating " +
           "FROM FeedbackAnswer fa WHERE fa.subject.id = :subjectId " +
           "GROUP BY fa.question.questionNumber ORDER BY fa.question.questionNumber")
    List<QuestionAverage> findAverageRatingsBySubject(Long subjectId);

    interface QuestionAverage {
        Integer getQNum();
        Double getAvgRating();
    }
}
