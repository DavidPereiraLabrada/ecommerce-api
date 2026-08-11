package com.davidpereiralabrada.ecommerceapi.service;

import com.davidpereiralabrada.ecommerceapi.model.CartItem;
import com.davidpereiralabrada.ecommerceapi.model.Product;
import com.davidpereiralabrada.ecommerceapi.repository.CartItemRepository;
import com.davidpereiralabrada.ecommerceapi.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CartCleanupTask {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    // Se ejecuta cada 15 minutos exactamente
    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void cleanupExpiredCarts() {
        LocalDateTime now = LocalDateTime.now();
        List<CartItem> expiredItems = cartItemRepository.findByExpiresAtBefore(now);

        for (CartItem item : expiredItems) {
            // Devolvuelve el stock al producto
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);

            // Elimina el ítem de la cesta
            cartItemRepository.delete(item);
        }

        if (!expiredItems.isEmpty()) {
            System.out.println("Limpieza de cesta completada: " + expiredItems.size() + " productos devueltos al stock.");
        }
    }
}