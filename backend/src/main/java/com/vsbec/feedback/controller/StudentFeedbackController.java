package com.vsbec.feedback.controller;

import com.vsbec.feedback.dto.FeedbackDtos.*;
import com.vsbec.feedback.entity.FeedbackQuestion;
import com.vsbec.feedback.service.FeedbackService;
import com.vsbec.feedback.util.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentFeedbackController {

    private final FeedbackService feedbackService;

    /** Subjects for the logged-in student's class, each flagged submitted/pending. */
    @GetMapping("/subjects")
    public List<SubjectFeedbackStatus> mySubjects(HttpServletRequest request) {
        Long studentId = RequestContext.studentId(request);
        return feedbackService.getSubjectStatusForStudent(studentId);
    }

    @GetMapping("/subjects/{subjectId}/questions")
    public List<FeedbackQuestion> questionsForSubject(@PathVariable Long subjectId) {
        return feedbackService.getQuestionsForSubject(subjectId);
    }

    @PostMapping("/feedback")
    public Map<String, String> submit(@Valid @RequestBody SubmitFeedbackRequest req, HttpServletRequest request) {
        Long studentId = RequestContext.studentId(request);
        feedbackService.submitFeedback(studentId, req);
        return Map.of("message", "Feedback submitted successfully. Thank you!");
    }
}
