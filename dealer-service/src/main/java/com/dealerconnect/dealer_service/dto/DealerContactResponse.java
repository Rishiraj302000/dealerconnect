package com.dealerconnect.dealer_service.dto;

import java.time.LocalDateTime;

public record DealerContactResponse(
        Long id,
        Long dealerId,
        String name,
        String email,
        String phone,
        String designation,
        LocalDateTime createdAt
) {
}
