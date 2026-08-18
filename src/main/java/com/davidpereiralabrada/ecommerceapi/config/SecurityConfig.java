package com.davidpereiralabrada.ecommerceapi.config;

// Comentamos estos imports si el CommandLineRunner está comentado para evitar avisos de "unused import"
/*
import com.davidpereiralabrada.ecommerceapi.model.Role;
import com.davidpereiralabrada.ecommerceapi.model.User;
import com.davidpereiralabrada.ecommerceapi.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
*/
import com.davidpereiralabrada.ecommerceapi.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Inyectamos las dependencias limpiamente por constructor
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, UserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .cors(org.springframework.security.config.Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable) // Desactivar CSRF ya que usamos APIs REST con tokens stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        // RUTAS PÚBLICAS (Sin token Bearer)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/images/**").permitAll()

                        //RUTA CLIENTES
                        .requestMatchers("/api/cart/**").hasAuthority("ROLE_CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/orders/checkout").hasAuthority("ROLE_CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/orders/my-orders").hasAuthority("ROLE_CLIENTE")

                        // RUTA DE CAJEROS Y ADMINS
                        .requestMatchers(HttpMethod.PATCH, "/api/products/*/stock").hasAnyAuthority("ROLE_ADMIN", "ROLE_CAJERO")
                        .requestMatchers(HttpMethod.GET, "/api/orders/all").hasAnyAuthority("ROLE_ADMIN", "ROLE_CAJERO")
                        .requestMatchers(HttpMethod.PUT, "/api/orders/*/status").hasAnyAuthority("ROLE_ADMIN", "ROLE_CAJERO")

                        // RUTAS DE ADMIN
                        .requestMatchers("/api/users/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/images/upload").hasAuthority("ROLE_ADMIN")

                        // CUALQUIER OTRA PETICIÓN (Con token Bearer)
                        .anyRequest().authenticated()
                )
                // Añadimos el filtro JWT antes de la validación de usuario y contraseña por defecto
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.List.of("http://localhost:3000")); // Tu frontend de Next.js
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /*
     * IMPORTANTE (ARRANQUE DESDE CERO):
     * Si clonas este repositorio y ejecutas la aplicación con una base de datos VACÍA
     * (sin usar el script SQL de Docker), descomenta este bloque temporalmente
     * para que se genere un usuario administrador inicial al arrancar.
     *
     * Credenciales que generará: admin / admin123

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ROLE_ADMIN);
                userRepository.save(admin);
                System.out.println(">>> Usuario administrador por defecto creado: admin / admin123");
            }
        };
    }
     */
}