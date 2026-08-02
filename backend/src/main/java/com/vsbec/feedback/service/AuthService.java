package com.vsbec.feedback.service;

import com.vsbec.feedback.dto.AuthDtos.*;
import com.vsbec.feedback.entity.Admin;
import com.vsbec.feedback.entity.Student;
import com.vsbec.feedback.exception.ApiException;
import com.vsbec.feedback.repository.AdminRepository;
import com.vsbec.feedback.repository.StudentRepository;
import com.vsbec.feedback.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.admin-expiry-ms}")
    private long adminExpiryMs;

    @Value("${app.jwt.student-expiry-ms}")
    private long studentExpiryMs;

    public LoginResponse adminLogin(AdminLoginRequest req) {
        Admin admin = adminRepository.findByUsernameAndActiveTrue(req.username())
                .orElseThrow(() -> ApiException.unauthorized("Invalid username or password"));

        if (!passwordEncoder.matches(req.password(), admin.getPasswordHash())) {
            throw ApiException.unauthorized("Invalid username or password");
        }

        String token = jwtUtil.generateToken(admin.getUsername(),
                Map.of("role", "ADMIN", "adminId", admin.getId(), "adminRole", admin.getRole().name()),
                adminExpiryMs);

        return new LoginResponse(token, "ADMIN", admin.getFullName(), admin.getId(), null, null);
    }

    /**
     * Students authenticate with register number only, per spec. No password.
     * This is intentionally a low-friction, single-purpose login: the resulting
     * token is short-lived and scoped to /api/student/** only.
     */
    public LoginResponse studentLogin(StudentLoginRequest req) {
        Student student = studentRepository.findByRegisterNumberIgnoreCaseAndActiveTrue(req.registerNumber())
                .orElseThrow(() -> ApiException.notFound("Register number not found. Contact your administrator."));

        String token = jwtUtil.generateToken(student.getRegisterNumber(),
                Map.of("role", "STUDENT", "studentId", student.getId(), "classId", student.getClassGroup().getId()),
                studentExpiryMs);

        return new LoginResponse(token, "STUDENT", student.getName(), student.getId(),
                student.getClassGroup().getId(), student.getClassGroup().getClassLabel());
    }
}
