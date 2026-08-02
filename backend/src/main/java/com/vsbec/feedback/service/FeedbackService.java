package com.vsbec.feedback.service;

import com.vsbec.feedback.dto.FeedbackDtos.*;
import com.vsbec.feedback.entity.*;
import com.vsbec.feedback.exception.ApiException;
import com.vsbec.feedback.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final FeedbackQuestionRepository questionRepository;
    private final FeedbackSubmissionRepository submissionRepository;
    private final FeedbackAnswerRepository answerRepository;

    /** Returns every subject in the student's class with a submitted/pending flag. Never exposes answers. */
    public List<SubjectFeedbackStatus> getSubjectStatusForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> ApiException.notFound("Student not found"));

        List<Subject> subjects = subjectRepository.findByClassGroup_IdOrderByDisplayOrderAsc(
                student.getClassGroup().getId());

        return subjects.stream().map(subject -> new SubjectFeedbackStatus(
                subject.getId(),
                subject.getSubjectName(),
                subject.getSubjectType().name(),
                subject.getFaculty().getName(),
                submissionRepository.existsByStudent_IdAndSubject_Id(studentId, subject.getId())
        )).collect(Collectors.toList());
    }

    public List<FeedbackQuestion> getQuestionsForSubject(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> ApiException.notFound("Subject not found"));
        return questionRepository.findBySubjectTypeAndActiveTrueOrderByQuestionNumberAsc(subject.getSubjectType());
    }

    /**
     * Records one student's feedback for one subject.
     * Enforces: feedback window open, exactly-once-per-subject, exactly 10 valid answers.
     * Writes the submission ledger row (identity-linked) and the answer rows
     * (NOT identity-linked) in the same transaction.
     */
    @Transactional
    public void submitFeedback(Long studentId, SubmitFeedbackRequest request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> ApiException.notFound("Student not found"));

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> ApiException.notFound("Subject not found"));

        if (!subject.getClassGroup().getId().equals(student.getClassGroup().getId())) {
            throw ApiException.forbidden("This subject does not belong to your class");
        }

        if (!subject.getClassGroup().isFeedbackOpen()) {
            throw ApiException.forbidden("Feedback is currently closed for your class");
        }

        if (submissionRepository.existsByStudent_IdAndSubject_Id(studentId, subject.getId())) {
            throw ApiException.conflict("You have already submitted feedback for this subject");
        }

        List<FeedbackQuestion> expectedQuestions =
                questionRepository.findBySubjectTypeAndActiveTrueOrderByQuestionNumberAsc(subject.getSubjectType());

        if (request.answers() == null || request.answers().size() != expectedQuestions.size()) {
            throw ApiException.badRequest("Exactly " + expectedQuestions.size() + " answers are required");
        }

        Map<Long, FeedbackQuestion> questionMap = expectedQuestions.stream()
                .collect(Collectors.toMap(FeedbackQuestion::getId, q -> q));

        for (var ans : request.answers()) {
            if (!questionMap.containsKey(ans.questionId())) {
                throw ApiException.badRequest("Invalid question for this subject: " + ans.questionId());
            }
            if (ans.rating() < 1 || ans.rating() > 5) {
                throw ApiException.badRequest("Rating must be between 1 and 5");
            }
        }

        // 1) Ledger row - identity linked, no answer content. DB unique constraint is the
        //    authoritative "once per subject" guard even under concurrent requests.
        FeedbackSubmission submission = FeedbackSubmission.builder()
                .student(student)
                .subject(subject)
                .build();
        try {
            submission = submissionRepository.saveAndFlush(submission);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw ApiException.conflict("You have already submitted feedback for this subject");
        }

        // 2) Answer rows - anonymous, only reference subject + question + rating + batch id.
        for (var ans : request.answers()) {
            FeedbackAnswer answer = FeedbackAnswer.builder()
                    .subject(subject)
                    .question(questionMap.get(ans.questionId()))
                    .rating(ans.rating())
                    .submissionBatchId(submission.getId())
                    .build();
            answerRepository.save(answer);
        }
    }
}
