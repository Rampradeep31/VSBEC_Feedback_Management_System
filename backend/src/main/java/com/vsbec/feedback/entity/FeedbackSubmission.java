package com.vsbec.feedback.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Ledger row proving "student X submitted feedback for subject Y".
 * This is the ONLY table that links a student identity to a feedback act.
 * It intentionally stores no answer content, which is what keeps
 * feedback_answers anonymous.
 */
@Entity
@Table(name = "feedback_submissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "subject_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedbackSubmission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    void onCreate() { submittedAt = LocalDateTime.now(); }
}
