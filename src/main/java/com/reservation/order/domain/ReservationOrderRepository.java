package com.reservation.order.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationOrderRepository extends JpaRepository<ReservationOrder, Long> {

    @EntityGraph(attributePaths = "items")
    Optional<ReservationOrder> findByIdAndMemberEmail(Long id, String email);

    @EntityGraph(attributePaths = "items")
    List<ReservationOrder> findAllByMemberEmailOrderByCreatedAtDesc(String email);
}
