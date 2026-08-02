package com.vsbec.feedback.controller;

import com.vsbec.feedback.dto.AuthDtos.*;
import com.vsbec.feedback.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/admin/login")
    public LoginResponse adminLogin(@Valid @RequestBody AdminLoginRequest req) {
        return authService.adminLogin(req);
    }

    @PostMapping("/student/login")
    public LoginResponse studentLogin(@Valid @RequestBody StudentLoginRequest req) {
        return authService.studentLogin(req);
    }
}
