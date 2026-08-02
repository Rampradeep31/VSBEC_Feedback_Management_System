package com.vsbec.feedback.service;

import com.vsbec.feedback.dto.AcademicDtos.*;
import com.vsbec.feedback.entity.*;
import com.vsbec.feedback.exception.ApiException;
import com.vsbec.feedback.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AcademicService {

    private final AcademicYearRepository academicYearRepository;
    private final DepartmentRepository departmentRepository;
    private final ClassGroupRepository classGroupRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;

    // ---------- Academic Year ----------
    public AcademicYear createAcademicYear(AcademicYearRequest req) {
        AcademicYear ay = AcademicYear.builder().yearLabel(req.yearLabel()).build();
        return academicYearRepository.save(ay);
    }

    public List<AcademicYear> listAcademicYears() {
        return academicYearRepository.findAll();
    }

    public void deleteAcademicYear(Long id) {
        academicYearRepository.deleteById(id);
    }

    // ---------- Department ----------
    public Department createDepartment(DepartmentRequest req) {
        Department dept = Department.builder().name(req.name()).shortCode(req.shortCode()).build();
        return departmentRepository.save(dept);
    }

    public List<Department> listDepartments() {
        return departmentRepository.findAll();
    }

    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }

    // ---------- Class ----------
    public ClassGroup createClass(ClassRequest req) {
        AcademicYear ay = academicYearRepository.findById(req.academicYearId())
                .orElseThrow(() -> ApiException.notFound("Academic year not found"));
        Department dept = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> ApiException.notFound("Department not found"));

        ClassGroup cg = ClassGroup.builder()
                .academicYear(ay)
                .department(dept)
                .yearOfStudy(ClassGroup.YearOfStudy.valueOf(req.yearOfStudy()))
                .section(req.section())
                .semester(ClassGroup.Semester.valueOf(req.semester()))
                .classLabel(req.classLabel())
                .feedbackOpen(false)
                .build();
        return classGroupRepository.save(cg);
    }

    public List<ClassGroup> listClasses() {
        return classGroupRepository.findAll();
    }

    public ClassGroup getClass(Long id) {
        return classGroupRepository.findById(id).orElseThrow(() -> ApiException.notFound("Class not found"));
    }

    public void deleteClass(Long id) {
        classGroupRepository.deleteById(id);
    }

    public ClassGroup toggleFeedback(Long classId, boolean open) {
        ClassGroup cg = getClass(classId);
        cg.setFeedbackOpen(open);
        if (open) {
            cg.setFeedbackOpenedAt(java.time.LocalDateTime.now());
        } else {
            cg.setFeedbackClosedAt(java.time.LocalDateTime.now());
        }
        return classGroupRepository.save(cg);
    }

    // ---------- Faculty ----------
    public Faculty createFaculty(FacultyRequest req) {
        Department dept = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> ApiException.notFound("Department not found"));
        Faculty f = Faculty.builder().name(req.name()).email(req.email()).department(dept).build();
        return facultyRepository.save(f);
    }

    public List<Faculty> listFaculty(Long departmentId) {
        return departmentId == null ? facultyRepository.findAll()
                : facultyRepository.findByDepartment_IdAndActiveTrue(departmentId);
    }

    public void deleteFaculty(Long id) {
        facultyRepository.deleteById(id);
    }

    // ---------- Subject ----------
    public Subject createSubject(SubjectRequest req) {
        ClassGroup cg = getClass(req.classId());
        Faculty faculty = facultyRepository.findById(req.facultyId())
                .orElseThrow(() -> ApiException.notFound("Faculty not found"));

        Subject subject = Subject.builder()
                .classGroup(cg)
                .faculty(faculty)
                .subjectName(req.subjectName())
                .subjectCode(req.subjectCode())
                .subjectType(SubjectType.valueOf(req.subjectType()))
                .displayOrder(req.displayOrder() == null ? 0 : req.displayOrder())
                .build();
        return subjectRepository.save(subject);
    }

    public List<Subject> listSubjectsForClass(Long classId) {
        return subjectRepository.findByClassGroup_IdOrderByDisplayOrderAsc(classId);
    }

    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
    }
}
