package com.dealerconnect.favorite_service.audit;

import com.dealerconnect.favorite_service.client.AuditClient;
import com.dealerconnect.favorite_service.client.dto.AuditRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Publishes favorite events to the Audit Service on a best-effort basis: a failure to
 * record an audit entry must never fail the favorite operation itself.
 */
@Component
public class AuditPublisher {

    private static final Logger log = LoggerFactory.getLogger(AuditPublisher.class);
    private static final String ENTITY_TYPE = "FAVORITE";

    private final AuditClient auditClient;

    public AuditPublisher(AuditClient auditClient) {
        this.auditClient = auditClient;
    }

    public void publish(String action, Long favoriteId, String performedBy, String details) {
        try {
            auditClient.record(new AuditRequest(action, ENTITY_TYPE, favoriteId, performedBy, details));
        } catch (Exception ex) {
            log.warn("Failed to publish audit event {} for favorite {}: {}", action, favoriteId, ex.getMessage());
        }
    }
}
