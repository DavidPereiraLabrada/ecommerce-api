package com.davidpereiralabrada.ecommerceapi.controller;

import com.davidpereiralabrada.ecommerceapi.dto.CartItemDTO;
import com.davidpereiralabrada.ecommerceapi.dto.ProductDTO;
import com.davidpereiralabrada.ecommerceapi.model.CartItem;
import com.davidpereiralabrada.ecommerceapi.model.User;
import com.davidpereiralabrada.ecommerceapi.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // Añadir producto a la cesta (Requiere estar logeado como cliente)
    @PostMapping("/add")
    public ResponseEntity<CartItem> addToCart(
            @AuthenticationPrincipal User user,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {

        CartItem cartItem = cartService.addToCart(user, productId, quantity);
        return ResponseEntity.ok(cartItem);
    }

    // Ver el contenido de la cesta del usuario actual
    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCart(@AuthenticationPrincipal User user) {
        List<CartItem> cartItems = cartService.getCartByUser(user);

        // Mapeamos las entidades CartItem y Product a sus respectivos DTOs
        List<CartItemDTO> dtoList = cartItems.stream().map(item -> {
            ProductDTO productDto = new ProductDTO(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getProduct().getDescription(),
                    item.getProduct().getPrice(),
                    item.getProduct().getStock(),
                    item.getProduct().getImageUrl()
            );

            return new CartItemDTO(
                    item.getId(),
                    productDto,
                    item.getQuantity(),
                    item.getExpiresAt()
            );
        }).toList();

        return ResponseEntity.ok(dtoList);
    }

    // Ver el precio total de la cesta
    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getCartTotal(@AuthenticationPrincipal User user) {
        BigDecimal total = cartService.calculateCartTotal(user);
        return ResponseEntity.ok(total);
    }

    // Elimina un producto de la cesta
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<String> removeFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId) {

        cartService.removeFromCart(user, cartItemId);
        return ResponseEntity.ok("Item removed from cart and stock returned successfully.");
    }
}