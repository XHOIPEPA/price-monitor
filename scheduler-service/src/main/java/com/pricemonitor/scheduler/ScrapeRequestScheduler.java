package com.pricemonitor.scheduler;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScrapeRequestScheduler {

    private static final Logger log = LoggerFactory.getLogger(ScrapeRequestScheduler.class);
    private static final String TOPIC = "scrape-requests";

    private final KafkaTemplate<String, ScrapeRequest> kafkaTemplate;

    // Fillimisht lista e produkteve eshte ne kod; me vone vjen nga databaza
    private final List<ScrapeRequest> products = List.of(
        new ScrapeRequest(1L, "https://books.toscrape.com/catalogue/a-light-in-the-attic_1000/index.html", null),
        new ScrapeRequest(2L, "https://books.toscrape.com/catalogue/tipping-the-velvet_999/index.html", null),
        new ScrapeRequest(3L, "https://books.toscrape.com/catalogue/soumission_998/index.html", null)
    );

    public ScrapeRequestScheduler(KafkaTemplate<String, ScrapeRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRateString = "${app.scrape-interval-ms}")
    public void publishScrapeRequests() {
        for (ScrapeRequest p : products) {
            ScrapeRequest request = new ScrapeRequest(p.productId(), p.url(), Instant.now().toString());
            // key = productId -> te gjitha mesazhet e nje produkti bien te i njejti partition
            kafkaTemplate.send(TOPIC, String.valueOf(request.productId()), request);
            log.info("Publikuar scrape-request per produktin {}", request.productId());
        }
    }
}