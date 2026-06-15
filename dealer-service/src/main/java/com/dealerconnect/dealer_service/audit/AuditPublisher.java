package com.dealerconnect.dealer_service.audit;

import com.dealerconnect.dealer_service.client.AuditClient;
import com.dealerconnect.dealer_service.client.dto.AuditRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Publishes dealer and contact events to the Audit Service on a best-effort basis:
 * auditing is a side concern, so a failure to record must never roll back or fail the
 * originating operation.
 */
@Component
public class AuditPublisher {

    private static final Logger log = LoggerFactory.getLogger(AuditPublisher.class);
    private static final String DEALER = "DEALER";
    private static final String CONTACT = "CONTACT";

    private final AuditClient auditClient;

    public AuditPublisher(AuditClient auditClient) {
        this.auditClient = auditClient;
    }

    public void publish(String action, Long dealerId, String performedBy, String details) {
        record(action, DEALER, dealerId, performedBy, details);
    }

    public void publishContact(String action, Long contactId, String performedBy, String details) {
        record(action, CONTACT, contactId, performedBy, details);
    }

    private void record(String action, String entityType, Long entityId, String performedBy, String details) {
        try {
            auditClient.record(new AuditRequest(action, entityType, entityId, performedBy, details));
        } catch (Exception ex) {
            log.warn("Failed to publish audit event {} for {} {}: {}", action, entityType, entityId, ex.getMessage());
        }
    }
}
