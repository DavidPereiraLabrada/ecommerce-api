package com.davidpereiralabrada.ecommerceapi.controller;

import com.davidpereiralabrada.ecommerceapi.dto.UserDTO;
import com.davidpereiralabrada.ecommerceapi.model.User;
import com.davidpereiralabrada.ecommerceapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Convertimos User a UserDTO
    private UserDTO convertToDTO(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getRole());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        // Convertimos toda la lista de Users a DTOs
        List<UserDTO> users = userRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setUsername(userDetails.getUsername());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        user.setRole(userDetails.getRole());

        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(convertToDTO(updatedUser)); // Devolvemos el DTO
    }
}