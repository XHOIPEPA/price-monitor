package com.pricemonitor.scheduler;

public record ScrapeRequest(Long productId, String url, String scheduledAt) {}