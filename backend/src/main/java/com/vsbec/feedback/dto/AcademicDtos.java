package com.vsbec.feedback.dto;

import jakarta.validation.constraints.NotBlank;

public class AcademicDtos {

    public record AcademicYearRequest(@NotBlank String yearLabel) {}

    public record DepartmentRequest(@NotBlank String name, @NotBlank String shortCode) {}

    public record ClassRequest(
            @NotBlank Long academicYearId,
            @NotBlank Long departmentId,
            @NotBlank String yearOfStudy,   // I, II, III, IV
            @NotBlank String section,
            @NotBlank String semester,      // ODD, EVEN
            @NotBlank String classLabel
    ) {}

    public record FacultyRequest(@NotBlank String name, String email, @NotBlank Long departmentId) {}

    public record SubjectRequest(
            @NotBlank Long classId,
            @NotBlank Long facultyId,
            @NotBlank String subjectName,
            String subjectCode,
            @NotBlank String subjectType,   // THEORY, LAB
            Integer displayOrder
    ) {}
}
