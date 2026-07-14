package com.pricemonitor.api;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductPriceHistoryRepository extends JpaRepository<ProductPriceHistory, Long> {

    List<ProductPriceHistory> findByProductIdOrderByRecordedAtDesc(Long productId);
}