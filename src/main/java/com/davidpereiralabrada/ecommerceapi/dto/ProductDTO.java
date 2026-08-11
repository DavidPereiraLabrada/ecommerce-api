package com.davidpereiralabrada.ecommerceapi.dto;

import java.math.BigDecimal;

public record ProductDTO(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String imageUrl
) {}