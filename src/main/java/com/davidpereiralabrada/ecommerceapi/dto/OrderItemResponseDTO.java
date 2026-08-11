package com.davidpereiralabrada.ecommerceapi.dto;

import java.math.BigDecimal;

public record OrderItemResponseDTO(
        Long id,
        ProductDTO product,
        Integer quantity,
        BigDecimal price
) {}