package com.davidpereiralabrada.ecommerceapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductDTO(
        Long id,

        @NotBlank(message = "The product name cannot be blank")
        String name,

        String description,

        @NotNull(message = "The price is required")
        @DecimalMin(value = "0.01", message = "The price must be greater than zero")
        BigDecimal price,

        @NotNull(message = "The stock quantity is required")
        @Min(value = 0, message = "The stock cannot be negative")
        Integer stock,

        String imageUrl
) {}