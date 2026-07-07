package com.pricemonitor.scraper;

public record ScrapeRequest(Long productId, String url, String scheduledAt) {}