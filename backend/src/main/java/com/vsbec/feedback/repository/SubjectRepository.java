package com.vsbec.feedback.repository;

import com.vsbec.feedback.entity.Subject;
import com.vsbec.feedback.entity.SubjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByClassGroup_IdOrderByDisplayOrderAsc(Long classId);
    List<Subject> findByClassGroup_IdAndSubjectTypeOrderByDisplayOrderAsc(Long classId, SubjectType subjectType);
}
