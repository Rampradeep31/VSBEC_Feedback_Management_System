package com.vsbec.feedback.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Anonymous answer row. Deliberately has NO student reference of any kind -
 * only which subject, which question, and the rating given. The
 * submission_batch_id groups the 10 answers of a single submission together
 * for insert/audit purposes only; it is never joined back to a student in
 * any service or controller.
 */
@Entity
@Table(name = "feedback_answers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedbackAnswer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "question_id", nullable = false)
    private FeedbackQuestion question;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "submission_batch_id", nullable = false)
    private Long submissionBatchId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}
