package com.davidpereiralabrada.ecommerceapi.service;

import com.davidpereiralabrada.ecommerceapi.model.*;
import com.davidpereiralabrada.ecommerceapi.repository.CartItemRepository;
import com.davidpereiralabrada.ecommerceapi.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    // Crea un pedido a partir de la cesta actual del cliente
    @Transactional
    public Order checkoutCart(User user) {

        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cannot checkout. The cart is empty.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING"); // Estado inicial para que lo vea el cajero
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());

            // Congelamos el precio actual del producto en el momento de la compra
            BigDecimal currentPrice = cartItem.getProduct().getPrice();
            orderItem.setPriceAtPurchase(currentPrice);

            orderItems.add(orderItem);

            BigDecimal itemTotal = currentPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        cartItemRepository.deleteAll(cartItems);

        return savedOrder;
    }

    // Pedidos del usuario
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    // Todos los pedidos (Para Admins o Cajeros)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Para que el cajero actualice el estado del pedido
    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {
        //Validamos que sea una petición válida
        if (!"COMPLETED".equals(newStatus) && !"CANCELLED".equals(newStatus)) {
            throw new IllegalArgumentException("Estado de pedido no válido: " + newStatus);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        //Evitamos modificar un pedido que ya ha sido completado
        if ("COMPLETED".equals(order.getStatus())) {
            throw new IllegalStateException("No se puede modificar un pedido que ya ha sido completado.");
        }

        // Si el cajero cancela el pedido, el stock debe devolverse al almacén
        if ("CANCELLED".equals(newStatus) && !"CANCELLED".equals(order.getStatus())) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
            }
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}