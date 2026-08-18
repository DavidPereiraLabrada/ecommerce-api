package com.davidpereiralabrada.ecommerceapi.dto;

import com.davidpereiralabrada.ecommerceapi.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserDTO(
        Long id,

        @NotBlank(message = "The username cannot be blank")
        String username,

        @NotNull(message = "The role is required")
        Role role
) {}