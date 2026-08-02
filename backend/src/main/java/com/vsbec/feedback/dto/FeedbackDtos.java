package com.vsbec.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class FeedbackDtos {

    public record QuestionAnswer(
            @NotNull Long questionId,
            @NotNull @Min(1) @Max(5) Integer rating
    ) {}

    public record SubmitFeedbackRequest(
            @NotNull Long subjectId,
            @NotNull List<QuestionAnswer> answers   // must contain exactly 10 answers
    ) {}

    public record SubjectFeedbackStatus(
            Long subjectId,
            String subjectName,
            String subjectType,
            String facultyName,
            boolean submitted
    ) {}
}
