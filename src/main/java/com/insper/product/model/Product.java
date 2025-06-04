package com.insper.product.model;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private Double price;
    private String unit;

    // Construtores
    public Product() {}

    public Product(String name, Double price, String unit) {
        this.name = name;
        this.price = price;
        this.unit = unit;
    }

    // Getters / Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
