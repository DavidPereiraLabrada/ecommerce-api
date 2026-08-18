package com.davidpereiralabrada.ecommerceapi.controller;

import com.davidpereiralabrada.ecommerceapi.dto.OrderItemResponseDTO;
import com.davidpereiralabrada.ecommerceapi.dto.OrderResponseDTO;
import com.davidpereiralabrada.ecommerceapi.dto.ProductDTO;
import com.davidpereiralabrada.ecommerceapi.dto.UserDTO;
import com.davidpereiralabrada.ecommerceapi.model.Order;
import com.davidpereiralabrada.ecommerceapi.model.User;
import com.davidpereiralabrada.ecommerceapi.service.OrderService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Cliente: Finaliza la compra de su cesta
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponseDTO> checkout(@AuthenticationPrincipal User user) {
        Order order = orderService.checkoutCart(user);
        return ResponseEntity.ok(convertToDto(order));
    }

    // Cliente: Ve sus propios pedidos
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(convertToDtoList(orderService.getOrdersByUser(user)));
    }

    // Cajero/Admin: Ve todos los pedidos pendientes o totales
    @GetMapping("/all")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(convertToDtoList(orderService.getAllOrders()));
    }

    // Cajero: Actualiza el estado de un pedido (COMPLETED, CANCELLED, etc.)
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateStatus(
            @PathVariable Long orderId,
            @RequestParam @NotBlank(message = "The status cannot be blank") String status) {
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(convertToDto(updatedOrder));
    }

    // Conversión a DTO
    private OrderResponseDTO convertToDto(Order order) {
        UserDTO userDto = new UserDTO(
                order.getUser().getId(),
                order.getUser().getUsername(),
                order.getUser().getRole()
        );

        List<OrderItemResponseDTO> itemDtos = order.getItems().stream().map(item ->
                new OrderItemResponseDTO(
                        item.getId(),
                        new ProductDTO(
                                item.getProduct().getId(),
                                item.getProduct().getName(),
                                item.getProduct().getDescription(),
                                item.getProduct().getPrice(),
                                item.getProduct().getStock(),
                                item.getProduct().getImageUrl()
                        ),
                        item.getQuantity(),
                        item.getPriceAtPurchase()
                )
        ).toList();

        return new OrderResponseDTO(order.getId(), order.getCreatedAt(), order.getStatus(), userDto, itemDtos);
    }

    private List<OrderResponseDTO> convertToDtoList(List<Order> orders) {
        return orders.stream().map(this::convertToDto).toList();
    }

}