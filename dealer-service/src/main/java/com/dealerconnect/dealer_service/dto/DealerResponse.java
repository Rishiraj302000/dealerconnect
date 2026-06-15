package com.dealerconnect.dealer_service.dto;

import com.dealerconnect.dealer_service.enums.DealerStatus;

import java.time.LocalDateTime;

public record DealerResponse(
        Long id,
        String name,
        String code,
        String email,
        String phone,
        String city,
        DealerStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
