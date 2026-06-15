package com.dealerconnect.favorite_service.dto;

import java.time.LocalDateTime;

public record FavoriteResponse(
        Long id,
        String username,
        Long dealerId,
        String dealerName,
        String dealerCode,
        String category,
        LocalDateTime createdAt
) {
}
