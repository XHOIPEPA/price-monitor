package com.pricemonitor.api;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCurrentStateRepository extends JpaRepository<ProductCurrentState, Long> {
}