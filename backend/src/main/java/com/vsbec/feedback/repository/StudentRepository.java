package com.vsbec.feedback.repository;

import com.vsbec.feedback.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRegisterNumberIgnoreCaseAndActiveTrue(String registerNumber);
    boolean existsByRegisterNumberIgnoreCase(String registerNumber);
    List<Student> findByClassGroup_Id(Long classId);
    long countByClassGroup_Id(Long classId);
}
