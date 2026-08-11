package com.davidpereiralabrada.ecommerceapi.dto;

import java.time.LocalDateTime;

public record CartItemDTO(
        Long id,
        ProductDTO product,
        Integer quantity,
        LocalDateTime expiresAt
) {}