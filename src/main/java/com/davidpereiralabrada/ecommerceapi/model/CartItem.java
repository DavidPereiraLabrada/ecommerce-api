package com.davidpereiralabrada.ecommerceapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Qué usuario tiene este producto en la cesta
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Qué producto está reservando
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Cuántas unidades ha metido en la cesta
    @Column(nullable = false)
    private Integer quantity;

    // Cuándo caduca esta reserva temporal
    @Column(nullable = false)
    private LocalDateTime expiresAt;
}