package com.vsbec.feedback.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "generated_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GeneratedReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "class_id", nullable = false)
    private ClassGroup classGroup;

    @Column(name = "docx_path", nullable = false, length = 500)
    private String docxPath;

    @Column(name = "pdf_path", nullable = false, length = 500)
    private String pdfPath;

    @Column(name = "generated_at", updatable = false)
    private LocalDateTime generatedAt;

    @Column(name = "generated_by")
    private Long generatedBy;

    @PrePersist
    void onCreate() { generatedAt = LocalDateTime.now(); }
}
