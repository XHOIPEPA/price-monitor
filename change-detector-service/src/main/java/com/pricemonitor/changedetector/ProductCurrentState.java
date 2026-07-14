package com.pricemonitor.changedetector;

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
        // JPA e kerkon nje konstruktor bosh
    }

    public ProductCurrentState(Long productId, Double price, String currency,
                               boolean inStock, Instant lastUpdated) {
        this.productId = productId;
        this.price = price;
        this.currency = currency;
        this.inStock = inStock;
        this.lastUpdated = lastUpdated;
    }

    public Long getProductId() { return productId; }
    public Double getPrice() { return price; }
    public String getCurrency() { return currency; }
    public boolean isInStock() { return inStock; }
    public Instant getLastUpdated() { return lastUpdated; }

    public void setPrice(Double price) { this.price = price; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
}