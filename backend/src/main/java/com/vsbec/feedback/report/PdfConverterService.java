package com.vsbec.feedback.report;

import com.vsbec.feedback.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Converts a generated DOCX report into PDF using a headless LibreOffice
 * install on the server, chosen for exact visual fidelity to the DOCX
 * (fonts, table borders, spacing render identically rather than through a
 * second, independent PDF-layout engine).
 *
 * Requires `soffice` (LibreOffice) to be installed and on PATH, or
 * app.reports.soffice-path pointed at the binary.
 */
@Service
public class PdfConverterService {

    @Value("${app.reports.soffice-path}")
    private String sofficePath;

    @Value("${app.reports.output-dir}")
    private String outputDir;

    public Path convertToPdf(Path docxPath) {
        try {
            Files.createDirectories(Path.of(outputDir));

            ProcessBuilder pb = new ProcessBuilder(
                    sofficePath, "--headless", "--norestore",
                    "--convert-to", "pdf",
                    "--outdir", outputDir,
                    docxPath.toAbsolutePath().toString());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Drain output to prevent the process blocking on a full pipe buffer
            try (var reader = process.getInputStream()) {
                reader.readAllBytes();
            }

            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw ApiException.badRequest("PDF conversion timed out");
            }
            if (process.exitValue() != 0) {
                throw ApiException.badRequest("PDF conversion failed (soffice exit code " + process.exitValue() + ")");
            }

            String pdfName = docxPath.getFileName().toString().replaceAll("\\.docx$", ".pdf");
            Path pdfPath = Path.of(outputDir, pdfName);
            if (!Files.exists(pdfPath)) {
                throw ApiException.badRequest("PDF conversion did not produce an output file");
            }
            return pdfPath;

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ApiException.badRequest("PDF conversion error: " + e.getMessage());
        }
    }
}
