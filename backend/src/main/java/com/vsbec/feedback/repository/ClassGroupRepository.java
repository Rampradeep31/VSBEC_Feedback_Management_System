package com.vsbec.feedback.repository;

import com.vsbec.feedback.entity.ClassGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClassGroupRepository extends JpaRepository<ClassGroup, Long> {
    List<ClassGroup> findByDepartment_Id(Long departmentId);
    List<ClassGroup> findByAcademicYear_Id(Long academicYearId);
}
