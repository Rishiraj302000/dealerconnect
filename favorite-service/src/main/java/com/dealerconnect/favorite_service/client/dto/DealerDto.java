package com.dealerconnect.favorite_service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Subset of the Dealer Service's response that the Favorite Service cares about.
 * Unknown fields are ignored so the contract stays loose.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DealerDto(
        Long id,
        String name,
        String code,
        String city,
        String status
) {
}
