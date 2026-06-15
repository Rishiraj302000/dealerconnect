package com.dealerconnect.dealer_service.client;

import com.dealerconnect.dealer_service.client.dto.AuditRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Sends business events to the Audit Service (service-to-service via Eureka).
 */
@FeignClient(name = "AUDIT-SERVICE")
public interface AuditClient {

    @PostMapping("/audit")
    void record(@RequestBody AuditRequest request);
}
