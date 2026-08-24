package com.vsbec.feedback.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "classes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ClassGroup {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "year_of_study", nullable = false, length = 5)
    private YearOfStudy yearOfStudy;

    @Column(nullable = false, length = 5)
    private String section;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Semester semester;

    @Column(name = "class_label", nullable = false, length = 50)
    private String classLabel;

    @Column(name = "is_feedback_open", nullable = false)
    @Builder.Default
    private boolean feedbackOpen = false;

    @Column(name = "feedback_opened_at")
    private LocalDateTime feedbackOpenedAt;

    @Column(name = "feedback_closed_at")
    private LocalDateTime feedbackClosedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }

    public enum YearOfStudy { I, II, III, IV }
    public enum Semester { ODD, EVEN }
}
