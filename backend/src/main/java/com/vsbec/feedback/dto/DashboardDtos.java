package com.vsbec.feedback.dto;

import java.util.List;

public class DashboardDtos {

    public record ClassDashboard(
            Long classId,
            String classLabel,
            long totalStudents,
            long feedbackSubmittedCount,
            long pendingCount,
            double completionPercentage,
            boolean feedbackOpen,
            List<FacultySummary> facultyList,
            List<SubjectSummary> subjectList
    ) {}

    public record FacultySummary(Long facultyId, String name, int subjectCount) {}

    public record SubjectSummary(Long subjectId, String subjectName, String subjectType,
                                  String facultyName, long responseCount) {}
}
