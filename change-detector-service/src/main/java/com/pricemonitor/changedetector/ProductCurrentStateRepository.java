package com.pricemonitor.changedetector;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface ProductCurrentStateRepository extends JpaRepository<ProductCurrentState, Long> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select s from ProductCurrentState s where s.productId = :id")
    Optional<ProductCurrentState> findFresh(@Param("id") Long id);
}