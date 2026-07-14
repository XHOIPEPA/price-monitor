package com.pricemonitor.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductCurrentStateRepository currentStateRepo;
    private final ProductPriceHistoryRepository historyRepo;

    public ProductController(ProductCurrentStateRepository currentStateRepo,
                             ProductPriceHistoryRepository historyRepo) {
        this.currentStateRepo = currentStateRepo;
        this.historyRepo = historyRepo;
    }

    // GET /api/products
    @GetMapping
    public List<ProductCurrentState> getAllProducts() {
        return currentStateRepo.findAll();
    }

    // GET /api/products/1
    @GetMapping("/{productId}")
    public ResponseEntity<ProductCurrentState> getProduct(@PathVariable Long productId) {
        return currentStateRepo.findById(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/products/1/history
    @GetMapping("/{productId}/history")
    public List<ProductPriceHistory> getHistory(@PathVariable Long productId) {
        return historyRepo.findByProductIdOrderByRecordedAtDesc(productId);
    }
}