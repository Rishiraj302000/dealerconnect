package com.dealerconnect.audit_service.service;

import com.dealerconnect.audit_service.dto.AuditRequest;
import com.dealerconnect.audit_service.dto.AuditResponse;
import com.dealerconnect.audit_service.entity.AuditRecord;
import com.dealerconnect.audit_service.repository.AuditRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private final AuditRecordRepository auditRecordRepository;

    public AuditService(AuditRecordRepository auditRecordRepository) {
        this.auditRecordRepository = auditRecordRepository;
    }

    @Transactional
    public AuditResponse record(AuditRequest request) {
        AuditRecord record = AuditRecord.builder()
                .action(request.action())
                .entityType(request.entityType())
                .entityId(request.entityId())
                .performedBy(request.performedBy())
                .details(request.details())
                .build();
        return toResponse(auditRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> history(String action, String entityType, String performedBy) {
        return auditRecordRepository.findHistory(action, entityType, performedBy)
                .stream().map(this::toResponse).toList();
    }

    private AuditResponse toResponse(AuditRecord record) {
        return new AuditResponse(
                record.getId(),
                record.getAction(),
                record.getEntityType(),
                record.getEntityId(),
                record.getPerformedBy(),
                record.getDetails(),
                record.getTimestamp());
    }
}
