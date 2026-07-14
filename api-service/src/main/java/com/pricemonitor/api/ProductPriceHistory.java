package com.pricemonitor.api;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_price_history")
public class ProductPriceHistory {

    @Id
    private Long id;

    private Long productId;
    private Double price;
    private String currency;
    private boolean inStock;
    private Instant recordedAt;

    protected ProductPriceHistory() {
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Double getPrice() { return price; }
    public String getCurrency() { return currency; }
    public boolean isInStock() { return inStock; }
    public Instant getRecordedAt() { return recordedAt; }
}