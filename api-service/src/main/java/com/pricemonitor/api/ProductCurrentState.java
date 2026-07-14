package com.pricemonitor.api;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_current_state")
public class ProductCurrentState {

    @Id
    private Long productId;

    private Double price;
    private String currency;
    private boolean inStock;
    private Instant lastUpdated;

    protected ProductCurrentState() {
    }

    public Long getProductId() { return productId; }
    public Double getPrice() { return price; }
    public String getCurrency() { return currency; }
    public boolean isInStock() { return inStock; }
    public Instant getLastUpdated() { return lastUpdated; }
}