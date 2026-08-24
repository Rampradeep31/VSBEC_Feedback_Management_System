package com.vsbec.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AcademicDtos {

    public record AcademicYearRequest(@NotBlank String yearLabel) {}

    public record DepartmentRequest(@NotBlank String name, @NotBlank String shortCode) {}

    public record ClassRequest(
            @NotNull Long academicYearId,
            @NotNull Long departmentId,
            @NotBlank String yearOfStudy,   // I, II, III, IV
            @NotBlank String section,
            @NotBlank String semester,      // ODD, EVEN
            @NotBlank String classLabel
    ) {}

    public record FacultyRequest(@NotBlank String name, String email, @NotNull Long departmentId) {}

    public record SubjectRequest(
            @NotNull Long classId,
            @NotNull Long facultyId,
            @NotBlank String subjectName,
            String subjectCode,
            @NotBlank String subjectType,   // THEORY, LAB
            Integer displayOrder
    ) {}
}
