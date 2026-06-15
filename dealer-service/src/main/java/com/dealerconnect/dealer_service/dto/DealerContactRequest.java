package com.dealerconnect.dealer_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DealerContactRequest(

        @NotNull(message = "Dealer id is required")
        Long dealerId,

        @NotBlank(message = "Name is required")
        String name,

        @Email(message = "Email must be valid")
        String email,

        String phone,

        String designation
) {
}
