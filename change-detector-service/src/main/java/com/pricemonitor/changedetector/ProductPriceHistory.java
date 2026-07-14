package com.pricemonitor.changedetector;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_price_history")
public class ProductPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private Double price;
    private String currency;
    private boolean inStock;
    private Instant recordedAt;

    protected ProductPriceHistory() {
    }

    public ProductPriceHistory(Long productId, Double price, String currency,
                               boolean inStock, Instant recordedAt) {
        this.productId = productId;
        this.price = price;
        this.currency = currency;
        this.inStock = inStock;
        this.recordedAt = recordedAt;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Double getPrice() { return price; }
    public Instant getRecordedAt() { return recordedAt; }
}