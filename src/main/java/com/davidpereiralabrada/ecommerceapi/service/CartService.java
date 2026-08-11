package com.davidpereiralabrada.ecommerceapi.service;

import com.davidpereiralabrada.ecommerceapi.model.CartItem;
import com.davidpereiralabrada.ecommerceapi.model.Product;
import com.davidpereiralabrada.ecommerceapi.model.User;
import com.davidpereiralabrada.ecommerceapi.repository.CartItemRepository;
import com.davidpereiralabrada.ecommerceapi.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    // Añadir producto a la cesta (reserva temporal)
    @Transactional
    public CartItem addToCart(User user, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Valida si hay suficiente stock físico
        if (product.getStock() < quantity) {
            throw new RuntimeException("Not enough stock available. Stock left: " + product.getStock());
        }

        // Resta el stock del producto de forma inmediata (reserva)
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        // Comprueba si el usuario ya tenía este producto en la cesta
        CartItem existingCartItem = cartItemRepository.findByUserAndProduct(user, product);

        if (existingCartItem != null) {
            // Si ya lo tenía, sumamos la cantidad y renovamos la expiración otros 15 minutos
            existingCartItem.setQuantity(existingCartItem.getQuantity() + quantity);
            existingCartItem.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            return cartItemRepository.save(existingCartItem);
        } else {
            // Si es nuevo en la cesta, creamos el registro
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            return cartItemRepository.save(newItem);
        }
    }

    // Ver la cesta de un usuario
    public List<CartItem> getCartByUser(User user) {
        return cartItemRepository.findByUser(user);
    }

    // Calcula el precio total de la cesta del usuario
    public BigDecimal calculateCartTotal(User user) {
        List<CartItem> items = cartItemRepository.findByUser(user);
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            BigDecimal quantityBigDecimal = BigDecimal.valueOf(item.getQuantity());
            BigDecimal itemTotal = item.getProduct().getPrice().multiply(quantityBigDecimal);
            total = total.add(itemTotal);
        }
        return total;
    }

    // Elimina un ítem de la cesta y devolver su stock inmediatamente
    @Transactional
    public void removeFromCart(User user, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Verifica que el ítem pertenece realmente al usuario autenticado por seguridad
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this cart item");
        }

        // Devuelve el stock al producto
        Product product = cartItem.getProduct();
        product.setStock(product.getStock() + cartItem.getQuantity());
        productRepository.save(product);

        // Elimina el ítem de la cesta
        cartItemRepository.delete(cartItem);
    }
}