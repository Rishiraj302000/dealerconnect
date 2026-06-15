package com.dealerconnect.favorite_service.dto;

import jakarta.validation.constraints.NotNull;

public record FavoriteRequest(

        @NotNull(message = "Dealer id is required")
        Long dealerId,

        String category
) {
}
