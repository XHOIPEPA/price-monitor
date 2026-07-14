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
    private final ProductRepository productRepository;

    public ScrapeRequestScheduler(KafkaTemplate<String, ScrapeRequest> kafkaTemplate,
                                  ProductRepository productRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.productRepository = productRepository;
    }

    @Scheduled(fixedRateString = "${app.scrape-interval-ms}")
    public void publishScrapeRequests() {
        List<Product> products = productRepository.findByActiveTrue();

        if (products.isEmpty()) {
            log.warn("Asnje produkt aktiv ne databaze — asgje per te skeduluar");
            return;
        }

        for (Product product : products) {
            ScrapeRequest request = new ScrapeRequest(
                    product.getId(), product.getUrl(), Instant.now().toString());

            // key = productId -> te gjitha mesazhet e nje produkti bien te i njejti partition
            kafkaTemplate.send(TOPIC, String.valueOf(request.productId()), request);
            log.info("Publikuar scrape-request per produktin {} ({})",
                    product.getId(), product.getName());
        }
    }
}