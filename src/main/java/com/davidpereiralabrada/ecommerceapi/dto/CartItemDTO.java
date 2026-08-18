package com.davidpereiralabrada.ecommerceapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CartItemDTO(
        Long id,

        @NotNull(message = "The product cannot be null")
        ProductDTO product,

        @NotNull(message = "The quantity is required")
        @Min(value = 1, message = "The quantity must be at least 1")
        Integer quantity,

        LocalDateTime expiresAt
) {}