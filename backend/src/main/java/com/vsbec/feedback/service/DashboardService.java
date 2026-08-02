package com.vsbec.feedback.service;

import com.vsbec.feedback.dto.DashboardDtos.*;
import com.vsbec.feedback.entity.ClassGroup;
import com.vsbec.feedback.entity.Subject;
import com.vsbec.feedback.exception.ApiException;
import com.vsbec.feedback.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ClassGroupRepository classGroupRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final FeedbackSubmissionRepository submissionRepository;

    public ClassDashboard getClassDashboard(Long classId) {
        ClassGroup cg = classGroupRepository.findById(classId)
                .orElseThrow(() -> ApiException.notFound("Class not found"));

        long totalStudents = studentRepository.countByClassGroup_Id(classId);
        long submitted = submissionRepository.countDistinctStudentsSubmittedForClass(classId);
        long pending = Math.max(0, totalStudents - submitted);
        double completion = totalStudents == 0 ? 0.0 : Math.round((submitted * 10000.0 / totalStudents)) / 100.0;

        List<Subject> subjects = subjectRepository.findByClassGroup_IdOrderByDisplayOrderAsc(classId);

        List<SubjectSummary> subjectSummaries = subjects.stream()
                .map(s -> new SubjectSummary(s.getId(), s.getSubjectName(), s.getSubjectType().name(),
                        s.getFaculty().getName(), submissionRepository.countBySubject_Id(s.getId())))
                .collect(Collectors.toList());

        Map<Long, List<Subject>> byFaculty = subjects.stream()
                .collect(Collectors.groupingBy(s -> s.getFaculty().getId()));

        List<FacultySummary> facultySummaries = byFaculty.entrySet().stream()
                .map(e -> new FacultySummary(e.getKey(), e.getValue().get(0).getFaculty().getName(), e.getValue().size()))
                .collect(Collectors.toList());

        return new ClassDashboard(classId, cg.getClassLabel(), totalStudents, submitted, pending,
                completion, cg.isFeedbackOpen(), facultySummaries, subjectSummaries);
    }
}
