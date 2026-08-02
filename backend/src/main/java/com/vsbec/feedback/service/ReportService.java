package com.vsbec.feedback.service;

import com.vsbec.feedback.entity.ClassGroup;
import com.vsbec.feedback.entity.GeneratedReport;
import com.vsbec.feedback.exception.ApiException;
import com.vsbec.feedback.report.PdfConverterService;
import com.vsbec.feedback.report.ReportGeneratorService;
import com.vsbec.feedback.repository.ClassGroupRepository;
import com.vsbec.feedback.repository.GeneratedReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ClassGroupRepository classGroupRepository;
    private final ReportGeneratorService reportGeneratorService;
    private final PdfConverterService pdfConverterService;
    private final GeneratedReportRepository generatedReportRepository;

    public record ReportPaths(Path docxPath, Path pdfPath, Long reportId) {}

    @Transactional
    public ReportPaths generateReport(Long classId, Long adminId) {
        ClassGroup cg = classGroupRepository.findById(classId)
                .orElseThrow(() -> ApiException.notFound("Class not found"));

        try {
            Path docxPath = reportGeneratorService.generateDocx(cg);
            Path pdfPath = pdfConverterService.convertToPdf(docxPath);

            GeneratedReport record = GeneratedReport.builder()
                    .classGroup(cg)
                    .docxPath(docxPath.toString())
                    .pdfPath(pdfPath.toString())
                    .generatedBy(adminId)
                    .build();
            record = generatedReportRepository.save(record);

            return new ReportPaths(docxPath, pdfPath, record.getId());
        } catch (IOException e) {
            throw ApiException.badRequest("Report generation failed: " + e.getMessage());
        }
    }
}
