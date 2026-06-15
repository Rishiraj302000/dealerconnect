package com.dealerconnect.favorite_service.client;

import com.dealerconnect.favorite_service.client.dto.DealerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Calls the Dealer Service directly (service-to-service via Eureka load balancing,
 * not through the gateway) to confirm a dealer exists before it can be favorited.
 */
@FeignClient(name = "DEALER-SERVICE")
public interface DealerClient {

    @GetMapping("/dealers/{id}")
    DealerDto getDealerById(@PathVariable("id") Long id);
}
