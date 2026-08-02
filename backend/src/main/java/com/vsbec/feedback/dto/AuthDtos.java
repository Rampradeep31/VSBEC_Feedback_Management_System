package com.vsbec.feedback.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthDtos {

    public record AdminLoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record StudentLoginRequest(@NotBlank String registerNumber) {}

    public record LoginResponse(String token, String role, String name, Long id, Long classId, String classLabel) {}
}
