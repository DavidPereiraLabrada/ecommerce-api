package com.davidpereiralabrada.ecommerceapi.dto;

import com.davidpereiralabrada.ecommerceapi.model.Role;

public class UserDTO {
    private Long id;
    private String username;
    private Role role;

    // Constructor vacío
    public UserDTO() {}

    // Constructor con parámetros
    public UserDTO(Long id, String username, Role role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}