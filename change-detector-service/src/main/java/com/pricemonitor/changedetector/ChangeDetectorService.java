package com.pricemonitor.changedetector;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeDetectorService {

    private static final Logger log = LoggerFactory.getLogger(ChangeDetectorService.class);
    private static final String OUTPUT_TOPIC = "price-events";

    private final ProductCurrentStateRepository currentStateRepo;
    private final ProductPriceHistoryRepository historyRepo;
    private final KafkaTemplate<String, PriceChangeEvent> kafkaTemplate;

    public ChangeDetectorService(ProductCurrentStateRepository currentStateRepo,
                                 ProductPriceHistoryRepository historyRepo,
                                 KafkaTemplate<String, PriceChangeEvent> kafkaTemplate) {
        this.currentStateRepo = currentStateRepo;
        this.historyRepo = historyRepo;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "product-snapshots", groupId = "change-detector-service")
    @Transactional
    public void handleSnapshot(ProductSnapshot snapshot) {
        Instant now = Instant.now();

        Optional<ProductCurrentState> existing = currentStateRepo.findFresh(snapshot.productId());

        if (existing.isEmpty()) {
            handleNewProduct(snapshot, now);
            return;
        }

        ProductCurrentState state = existing.get();
        Double oldPrice = state.getPrice();
        boolean oldInStock = state.isInStock();

        log.info("KRAHASIM produkt={}: DB(cmim={}, stock={}) vs SNAPSHOT(cmim={}, stock={})",
                snapshot.productId(), oldPrice, oldInStock, snapshot.price(), snapshot.inStock());

        boolean priceChanged = !Objects.equals(oldPrice, snapshot.price());
        boolean stockChanged = oldInStock != snapshot.inStock();

        if (!priceChanged && !stockChanged) {
            // Asnje ndryshim: perditesojme vetem kohen e kontrollit te fundit
            state.setLastUpdated(now);
            currentStateRepo.save(state);
            log.debug("Pa ndryshim per produktin {}", snapshot.productId());
            return;
        }

        // Ka ndryshim: ruajme ne historik
        historyRepo.save(new ProductPriceHistory(
                snapshot.productId(), snapshot.price(), snapshot.currency(),
                snapshot.inStock(), now));

        // Perditesojme gjendjen aktuale
        state.setPrice(snapshot.price());
        state.setCurrency(snapshot.currency());
        state.setInStock(snapshot.inStock());
        state.setLastUpdated(now);
        currentStateRepo.save(state);

        // Publikojme ngjarjen
        PriceChangeEvent event = new PriceChangeEvent(
                snapshot.productId(),
                oldPrice,
                snapshot.price(),
                changePercent(oldPrice, snapshot.price()),
                oldInStock,
                snapshot.inStock(),
                changeType(oldPrice, snapshot.price(), priceChanged, stockChanged),
                snapshot.currency(),
                now.toString());

        kafkaTemplate.send(OUTPUT_TOPIC, String.valueOf(snapshot.productId()), event);

        log.info("NDRYSHIM! produkt={} {} -> {} ({}) tip={}",
                event.productId(), event.oldPrice(), event.newPrice(),
                event.changePercent() != null ? String.format("%.2f%%", event.changePercent()) : "n/a",
                event.changeType());
    }

    private void handleNewProduct(ProductSnapshot snapshot, Instant now) {
        historyRepo.save(new ProductPriceHistory(
                snapshot.productId(), snapshot.price(), snapshot.currency(),
                snapshot.inStock(), now));

        currentStateRepo.save(new ProductCurrentState(
                snapshot.productId(), snapshot.price(), snapshot.currency(),
                snapshot.inStock(), now));

        log.info("Produkt i ri: {} me cmim {} {}",
                snapshot.productId(), snapshot.price(), snapshot.currency());
    }

    private static Double changePercent(Double oldPrice, Double newPrice) {
        if (oldPrice == null || newPrice == null || oldPrice == 0.0) return null;
        return ((newPrice - oldPrice) / oldPrice) * 100.0;
    }

    private static String changeType(Double oldPrice, Double newPrice,
                                     boolean priceChanged, boolean stockChanged) {
        if (priceChanged && oldPrice != null && newPrice != null) {
            return newPrice < oldPrice ? "PRICE_DROP" : "PRICE_INCREASE";
        }
        if (stockChanged) return "STOCK_CHANGE";
        return "UNKNOWN";
    }
}