package com.pricemonitor.changedetector;

public record ProductSnapshot(
        Long productId,
        String title,
        Double price,
        String currency,
        boolean inStock,
        String scrapedAt) {}