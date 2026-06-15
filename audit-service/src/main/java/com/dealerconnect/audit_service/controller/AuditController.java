package com.dealerconnect.audit_service.controller;

import com.dealerconnect.audit_service.dto.AuditRequest;
import com.dealerconnect.audit_service.dto.AuditResponse;
import com.dealerconnect.audit_service.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Called service-to-service (via Feign) by the Dealer and Favorite services to
     * record a business event. Not intended for direct external use.
     */
    @PostMapping
    public ResponseEntity<AuditResponse> record(@Valid @RequestBody AuditRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auditService.record(request));
    }

    /**
     * Audit history for administrators, newest first, with optional filters.
     */
    @GetMapping
    public ResponseEntity<List<AuditResponse>> history(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String performedBy) {
        return ResponseEntity.ok(auditService.history(action, entityType, performedBy));
    }
}
