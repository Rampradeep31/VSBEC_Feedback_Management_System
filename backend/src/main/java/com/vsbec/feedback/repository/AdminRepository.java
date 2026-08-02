package com.vsbec.feedback.repository;

import com.vsbec.feedback.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsernameAndActiveTrue(String username);
}
