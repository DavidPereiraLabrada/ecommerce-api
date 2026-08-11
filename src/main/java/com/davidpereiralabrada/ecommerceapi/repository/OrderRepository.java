package com.davidpereiralabrada.ecommerceapi.repository;

import com.davidpereiralabrada.ecommerceapi.model.Order;
import com.davidpereiralabrada.ecommerceapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}