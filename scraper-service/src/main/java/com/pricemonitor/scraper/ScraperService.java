package com.pricemonitor.scraper;

import java.time.Instant;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ScraperService {

    private static final Logger log = LoggerFactory.getLogger(ScraperService.class);
    private static final String OUTPUT_TOPIC = "product-snapshots";

    private final KafkaTemplate<String, ProductSnapshot> kafkaTemplate;

    public ScraperService(KafkaTemplate<String, ProductSnapshot> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "scrape-requests", groupId = "scraper-service")
    public void handleScrapeRequest(ScrapeRequest request) {
        log.info("Marre scrape-request per produktin {} ({})", request.productId(), request.url());
        try {
            Document doc = Jsoup.connect(request.url())
                    .userAgent("Mozilla/5.0 (price-monitor thesis project)")
                    .timeout(10000)
                    .get();

            String title = text(doc, "div.product_main h1");
            String priceRaw = text(doc, "p.price_color");       // p.sh. "£51.77"
            String availability = text(doc, "p.availability");   // p.sh. "In stock (22 available)"

            Double price = parsePrice(priceRaw);
            boolean inStock = availability != null
                    && availability.toLowerCase().contains("in stock");

            ProductSnapshot snapshot = new ProductSnapshot(
                    request.productId(), title, price, "GBP", inStock, Instant.now().toString());

            kafkaTemplate.send(OUTPUT_TOPIC, String.valueOf(request.productId()), snapshot);
            log.info("Publikuar snapshot: produkt={} titull='{}' cmim={} inStock={}",
                    snapshot.productId(), snapshot.title(), snapshot.price(), snapshot.inStock());

        } catch (Exception e) {
            log.error("Deshtoi scraping per produktin {}: {}", request.productId(), e.getMessage());
        }
    }

    private static String text(Document doc, String cssQuery) {
        var el = doc.selectFirst(cssQuery);
        return el != null ? el.text().trim() : null;
    }

    private static Double parsePrice(String raw) {
        if (raw == null) return null;
        String cleaned = raw.replaceAll("[^0-9.]", "");   // heq £ dhe simbole te tjera
        return cleaned.isEmpty() ? null : Double.parseDouble(cleaned);
    }
}