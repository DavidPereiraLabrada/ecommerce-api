package com.davidpereiralabrada.ecommerceapi.repository;

import com.davidpereiralabrada.ecommerceapi.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Método derivado por convención de nombres (Derived Query)
    List<Product> findByNameContainingIgnoreCase(String name);
}