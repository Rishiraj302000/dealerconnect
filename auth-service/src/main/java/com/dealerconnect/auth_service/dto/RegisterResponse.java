package com.dealerconnect.auth_service.dto;

import com.dealerconnect.auth_service.enums.Role;

import java.time.LocalDateTime;

public record RegisterResponse(
        Long id,
        String name,
        String email,
        Role role,
        LocalDateTime createdAt
) {
}
