package com.dealerconnect.favorite_service.client.dto;

/**
 * Payload sent to the Audit Service when a favorite business event occurs.
 */
public record AuditRequest(
        String action,
        String entityType,
        Long entityId,
        String performedBy,
        String details
) {
}
