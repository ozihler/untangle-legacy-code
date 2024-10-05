package com.codeartify.tablebooking.repository;

import com.codeartify.tablebooking.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByReservedBy(String reservedBy);
}
