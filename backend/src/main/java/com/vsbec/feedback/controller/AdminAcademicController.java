package com.vsbec.feedback.controller;

import com.vsbec.feedback.dto.AcademicDtos.*;
import com.vsbec.feedback.entity.*;
import com.vsbec.feedback.service.AcademicService;
import com.vsbec.feedback.service.StudentImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAcademicController {

    private final AcademicService academicService;
    private final StudentImportService studentImportService;

    // ---------- Academic Years ----------
    @PostMapping("/academic-years")
    public AcademicYear createAcademicYear(@Valid @RequestBody AcademicYearRequest req) {
        return academicService.createAcademicYear(req);
    }

    @GetMapping("/academic-years")
    public List<AcademicYear> listAcademicYears() {
        return academicService.listAcademicYears();
    }

    @DeleteMapping("/academic-years/{id}")
    public void deleteAcademicYear(@PathVariable Long id) {
        academicService.deleteAcademicYear(id);
    }

    // ---------- Departments ----------
    @PostMapping("/departments")
    public Department createDepartment(@Valid @RequestBody DepartmentRequest req) {
        return academicService.createDepartment(req);
    }

    @GetMapping("/departments")
    public List<Department> listDepartments() {
        return academicService.listDepartments();
    }

    @DeleteMapping("/departments/{id}")
    public void deleteDepartment(@PathVariable Long id) {
        academicService.deleteDepartment(id);
    }

    // ---------- Classes ----------
    @PostMapping("/classes")
    public ClassGroup createClass(@Valid @RequestBody ClassRequest req) {
        return academicService.createClass(req);
    }

    @GetMapping("/classes")
    public List<ClassGroup> listClasses() {
        return academicService.listClasses();
    }

    @GetMapping("/classes/{id}")
    public ClassGroup getClass(@PathVariable Long id) {
        return academicService.getClass(id);
    }

    @DeleteMapping("/classes/{id}")
    public void deleteClass(@PathVariable Long id) {
        academicService.deleteClass(id);
    }

    @PatchMapping("/classes/{id}/feedback-window")
    public ClassGroup toggleFeedback(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        return academicService.toggleFeedback(id, Boolean.TRUE.equals(body.get("open")));
    }

    // ---------- Faculty ----------
    @PostMapping("/faculty")
    public Faculty createFaculty(@Valid @RequestBody FacultyRequest req) {
        return academicService.createFaculty(req);
    }

    @GetMapping("/faculty")
    public List<Faculty> listFaculty(@RequestParam(required = false) Long departmentId) {
        return academicService.listFaculty(departmentId);
    }

    @DeleteMapping("/faculty/{id}")
    public void deleteFaculty(@PathVariable Long id) {
        academicService.deleteFaculty(id);
    }

    // ---------- Subjects ----------
    @PostMapping("/subjects")
    public Subject createSubject(@Valid @RequestBody SubjectRequest req) {
        return academicService.createSubject(req);
    }

    @GetMapping("/classes/{classId}/subjects")
    public List<Subject> listSubjectsForClass(@PathVariable Long classId) {
        return academicService.listSubjectsForClass(classId);
    }

    @DeleteMapping("/subjects/{id}")
    public void deleteSubject(@PathVariable Long id) {
        academicService.deleteSubject(id);
    }

    // ---------- Student bulk upload ----------
    @PostMapping(value = "/classes/{classId}/students/upload", consumes = "multipart/form-data")
    public StudentImportService.ImportResult uploadStudents(@PathVariable Long classId,
                                                             @RequestParam("file") MultipartFile file) {
        ClassGroup cg = academicService.getClass(classId);
        return studentImportService.importStudents(file, cg);
    }
}
