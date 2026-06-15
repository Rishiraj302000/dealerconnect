package com.dealerconnect.audit_service.dto;

import jakarta.validation.constraints.NotBlank;

public record AuditRequest(

        @NotBlank(message = "Action is required")
        String action,

        @NotBlank(message = "Entity type is required")
        String entityType,

        Long entityId,

        String performedBy,

        String details
) {
}
