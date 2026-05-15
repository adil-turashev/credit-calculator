package org.example.credit4.repository;

import org.example.credit4.entity.CreditRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditRequestRepository extends JpaRepository<CreditRequestEntity, Long> {
    List<CreditRequestEntity> findByUserUuid(String userUuid);
}