package com.vsbec.feedback.repository;

import com.vsbec.feedback.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
