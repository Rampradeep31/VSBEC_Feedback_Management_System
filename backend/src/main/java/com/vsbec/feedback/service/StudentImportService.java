package com.vsbec.feedback.service;

import com.vsbec.feedback.entity.ClassGroup;
import com.vsbec.feedback.entity.Student;
import com.vsbec.feedback.exception.ApiException;
import com.vsbec.feedback.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Bulk student upload via Excel.
 * Expected columns (header row required, order-independent, case-insensitive):
 *   RegisterNumber | Name | Email (optional)
 */
@Service
@RequiredArgsConstructor
public class StudentImportService {

    private final StudentRepository studentRepository;

    public record ImportResult(int totalRows, int imported, int skippedDuplicates, List<String> errors) {}

    @Transactional
    public ImportResult importStudents(MultipartFile file, ClassGroup classGroup) {
        List<String> errors = new ArrayList<>();
        int imported = 0, skipped = 0, totalRows = 0;

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw ApiException.badRequest("Excel file has no header row");
            }

            int regNoCol = -1, nameCol = -1, emailCol = -1;
            for (Cell cell : headerRow) {
                String header = cell.getStringCellValue().trim().toLowerCase();
                if (header.contains("register")) regNoCol = cell.getColumnIndex();
                else if (header.contains("name")) nameCol = cell.getColumnIndex();
                else if (header.contains("email")) emailCol = cell.getColumnIndex();
            }
            if (regNoCol == -1 || nameCol == -1) {
                throw ApiException.badRequest("Excel must have 'RegisterNumber' and 'Name' columns");
            }

            DataFormatter formatter = new DataFormatter();

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String regNo = formatter.formatCellValue(row.getCell(regNoCol)).trim();
                String name = formatter.formatCellValue(row.getCell(nameCol)).trim();
                if (regNo.isEmpty() && name.isEmpty()) continue;

                totalRows++;

                if (regNo.isEmpty()) {
                    errors.add("Row " + (r + 1) + ": missing register number");
                    continue;
                }
                if (studentRepository.existsByRegisterNumberIgnoreCase(regNo)) {
                    skipped++;
                    continue;
                }

                String email = emailCol == -1 ? null : formatter.formatCellValue(row.getCell(emailCol)).trim();

                Student student = Student.builder()
                        .registerNumber(regNo)
                        .name(name.isEmpty() ? regNo : name)
                        .email(email == null || email.isEmpty() ? null : email)
                        .classGroup(classGroup)
                        .active(true)
                        .build();
                studentRepository.save(student);
                imported++;
            }
        } catch (IOException e) {
            throw ApiException.badRequest("Could not read Excel file: " + e.getMessage());
        }

        return new ImportResult(totalRows, imported, skipped, errors);
    }
}
