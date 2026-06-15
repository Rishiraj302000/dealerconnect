package com.dealerconnect.audit_service.dto;

import java.time.LocalDateTime;

public record AuditResponse(
        Long id,
        String action,
        String entityType,
        Long entityId,
        String performedBy,
        String details,
        LocalDateTime timestamp
) {
}
