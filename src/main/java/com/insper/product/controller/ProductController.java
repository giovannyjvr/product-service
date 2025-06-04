package com.insper.product.controller;

import com.insper.product.model.Product;
import com.insper.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService service;

    // LISTAR TODOS (pode ser público ou exigir TOKEN, conforme enunciado)
    @GetMapping
    public List<Product> getAll() {
        return service.findAll();
    }

    // BUSCAR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable UUID id) {
        Optional<Product> maybe = service.findById(id);
        return maybe.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // CRIAR → geralmente exige token válido (exemplo abaixo com @PreAuthorize)
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")  // Exemplo: apenas ADMIN pode criar
    public ResponseEntity<Product> create(@RequestBody Product product) {
        Product saved = service.save(product);
        URI location = URI.create("/products/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    // ATUALIZAR
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Product> update(@PathVariable UUID id,
                                          @RequestBody Product product) {
        Optional<Product> maybe = service.findById(id);
        if (maybe.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        product.setId(id);
        Product updated = service.save(product);
        return ResponseEntity.ok(updated);
    }

    // DELETAR
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        Optional<Product> maybe = service.findById(id);
        if (maybe.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
