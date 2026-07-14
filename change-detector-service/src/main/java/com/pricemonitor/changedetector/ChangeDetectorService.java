package com.pricemonitor.changedetector;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeDetectorService {

    private static final Logger log = LoggerFactory.getLogger(ChangeDetectorService.class);

    private final ProductCurrentStateRepository currentStateRepo;
    private final ProductPriceHistoryRepository historyRepo;

    public ChangeDetectorService(ProductCurrentStateRepository currentStateRepo,
                                 ProductPriceHistoryRepository historyRepo) {
        this.currentStateRepo = currentStateRepo;
        this.historyRepo = historyRepo;
    }

    @KafkaListener(topics = "product-snapshots", groupId = "change-detector-service")
    @Transactional
    public void handleSnapshot(ProductSnapshot snapshot) {
        log.info("Marre snapshot: produkt={} cmim={} inStock={}",
                snapshot.productId(), snapshot.price(), snapshot.inStock());

        Instant now = Instant.now();

        // Ruajme ne historik cdo snapshot (per momentin; ne Diten 4 vetem kur ndryshon)
        historyRepo.save(new ProductPriceHistory(
                snapshot.productId(), snapshot.price(), snapshot.currency(),
                snapshot.inStock(), now));

        // Perditesojme gjendjen aktuale (ose e krijojme nese produkti eshte i ri)
        ProductCurrentState state = currentStateRepo.findById(snapshot.productId())
                .orElseGet(() -> new ProductCurrentState(
                        snapshot.productId(), snapshot.price(), snapshot.currency(),
                        snapshot.inStock(), now));

        state.setPrice(snapshot.price());
        state.setCurrency(snapshot.currency());
        state.setInStock(snapshot.inStock());
        state.setLastUpdated(now);

        currentStateRepo.save(state);

        log.info("Ruajtur ne DB: produkt={}", snapshot.productId());
    }
}