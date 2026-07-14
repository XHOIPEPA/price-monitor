package com.pricemonitor.changedetector;

public record ProductSnapshot(
        Long productId,
        Double price,
        String currency,
        boolean inStock,
        String scrapedAt) {}