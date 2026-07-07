package com.pricemonitor.scraper;

public record ProductSnapshot(
        Long productId,
        Double price,
        String currency,
        boolean inStock,
        String scrapedAt) {}