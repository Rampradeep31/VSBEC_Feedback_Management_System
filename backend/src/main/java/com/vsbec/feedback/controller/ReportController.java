package com.vsbec.feedback.controller;

import com.vsbec.feedback.service.ReportService;
import com.vsbec.feedback.util.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /** Generates fresh DOCX + PDF for the class and returns their paths / id. */
    @PostMapping("/classes/{classId}/generate")
    public ReportService.ReportPaths generate(@PathVariable Long classId, HttpServletRequest request) {
        Long adminId = RequestContext.adminId(request);
        return reportService.generateReport(classId, adminId);
    }

    @PostMapping("/classes/{classId}/generate/docx/download")
    public ResponseEntity<FileSystemResource> generateAndDownloadDocx(@PathVariable Long classId, HttpServletRequest request) {
        Long adminId = RequestContext.adminId(request);
        var paths = reportService.generateReport(classId, adminId);
        FileSystemResource resource = new FileSystemResource(paths.docxPath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + paths.docxPath().getFileName() + "\"")
                .body(resource);
    }

    @PostMapping("/classes/{classId}/generate/pdf/download")
    public ResponseEntity<FileSystemResource> generateAndDownloadPdf(@PathVariable Long classId, HttpServletRequest request) {
        Long adminId = RequestContext.adminId(request);
        var paths = reportService.generateReport(classId, adminId);
        FileSystemResource resource = new FileSystemResource(paths.pdfPath());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + paths.pdfPath().getFileName() + "\"")
                .body(resource);
    }
}
