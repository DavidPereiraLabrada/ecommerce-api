package com.davidpereiralabrada.ecommerceapi.controller;

import com.davidpereiralabrada.ecommerceapi.dto.LoginRequestDTO;
import com.davidpereiralabrada.ecommerceapi.dto.RegisterRequestDTO;
import com.davidpereiralabrada.ecommerceapi.model.Role;
import com.davidpereiralabrada.ecommerceapi.model.User;
import com.davidpereiralabrada.ecommerceapi.repository.UserRepository;
import com.davidpereiralabrada.ecommerceapi.security.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        String username = request.username();
        String password = request.password();

        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }

        String token = jwtUtils.generateToken(user.getUsername(), user.getRole());

        return ResponseEntity.ok(Map.of(
                "token_type", "Bearer",
                "access_token", token,
                "expires_in", "60 minutos",
                "role", user.getRole().name()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO request, Authentication authentication) {
        String username = request.username();
        String password = request.password();
        String requestedRole = request.role(); // El rol que intentan crear (opcional)

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario ya existe"));
        }

        Role finalRole;

        // CASO 1: Es un usuario anónimo (sin token / no logueado) registrándose en la tienda
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // Si no está autenticado, por narices es un cliente (ignoramos si mandó otro rol)
            finalRole = Role.ROLE_CLIENTE;
        }
        // CASO 2: Hay alguien autenticado intentando crear un usuario desde dentro
        else {
            // Obtenemos los roles del usuario que está haciendo la petición
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

            boolean isCajero = authentication.getAuthorities().stream()
                    .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_CAJERO"));

            // Si es un CAJERO, tiene prohibido crear cajeros u otros admins
            if (isCajero && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Los cajeros no tienen permisos para crear otros usuarios con roles administrativos o de cajero."));
            }

            // Si es un ADMIN, puede elegir qué rol crear
            if (isAdmin) {
                if (requestedRole == null || requestedRole.isEmpty()) {
                    finalRole = Role.ROLE_CLIENTE; // Por defecto si el admin no especifica
                } else {
                    try {
                        Role parsedRole = Role.valueOf(requestedRole.toUpperCase());
                        // Validamos que el admin introduzca un rol válido existente
                        if (parsedRole == Role.ROLE_ADMIN || parsedRole == Role.ROLE_CAJERO || parsedRole == Role.ROLE_CLIENTE) {
                            finalRole = parsedRole;
                        } else {
                            return ResponseEntity.badRequest().body(Map.of("error", "Rol inválido."));
                        }
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(Map.of("error", "El rol especificado no existe. Usa ROLE_ADMIN, ROLE_CAJERO o ROLE_CLIENTE"));
                    }
                }
            } else {
                // Si por alguna razón es otro tipo de usuario autenticado pero sin permisos
                finalRole = Role.ROLE_CLIENTE;
            }
        }

        // Creamos y guardamos el usuario con el rol determinado de forma segura
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(finalRole);

        userRepository.save(newUser);

        return ResponseEntity.ok(Map.of(
                "message", "Usuario registrado exitosamente",
                "username", username,
                "role", finalRole.name()
        ));
    }
}