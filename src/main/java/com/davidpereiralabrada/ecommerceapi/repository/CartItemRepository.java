package com.davidpereiralabrada.ecommerceapi.repository;

import com.davidpereiralabrada.ecommerceapi.model.CartItem;
import com.davidpereiralabrada.ecommerceapi.model.Product;
import com.davidpereiralabrada.ecommerceapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Busca todos los ítems en la cesta de un usuario específico
    List<CartItem> findByUser(User user);

    // Busca ítems cuya fecha de expiración ya haya pasado (para la limpieza de la cesta)
    List<CartItem> findByExpiresAtBefore(LocalDateTime now);

    // Busca si un usuario ya tiene ese producto en la cesta para sumarlo en lugar de duplicarlo
    CartItem findByUserAndProduct(User user, Product product);
}