package com.davidpereiralabrada.ecommerceapi.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
        Long id,
        LocalDateTime createdAt,
        String status,
        UserDTO user,
        List<OrderItemResponseDTO> items
) {}