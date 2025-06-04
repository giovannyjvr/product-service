package com.insper.product.repository;

import com.insper.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    // Aqui você pode adicionar consultas customizadas se necessário
}
