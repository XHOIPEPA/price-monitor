package com.pricemonitor.changedetector;

public record PriceChangeEvent(
        Long productId,
        Double oldPrice,
        Double newPrice,
        Double changePercent,
        boolean oldInStock,
        boolean newInStock,
        String changeType,
        String currency,
        String detectedAt) {}