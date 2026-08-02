package com.vsbec.feedback.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feedback_questions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedbackQuestion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false, length = 10)
    private SubjectType subjectType;

    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
