package org.example.credit4.repository;

import org.example.credit4.entity.CreditRequestEntity;
import org.example.credit4.entity.CreditRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditRequestRepository extends JpaRepository<CreditRequestEntity, Long> {
    List<CreditRequestEntity> findByUserUuidOrderByRequestedAtDesc(String userUuid);
    List<CreditRequestEntity> findAllByOrderByRequestedAtDesc();
    long countByStatus(CreditRequestStatus status);
}