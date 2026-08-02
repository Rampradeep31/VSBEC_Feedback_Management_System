package com.vsbec.feedback.repository;

import com.vsbec.feedback.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    List<Faculty> findByDepartment_IdAndActiveTrue(Long departmentId);
}
