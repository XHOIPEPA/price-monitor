package com.pricemonitor.scraper;

public record ProductSnapshot(
        Long productId,
        String title,
        Double price,
        String currency,
        boolean inStock,
        String scrapedAt) {}