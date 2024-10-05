package com.codeartify.tablebooking.repository;

import com.codeartify.tablebooking.model.Desk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeskRepository extends JpaRepository<Desk, Long> {
    List<Desk> findByAvailable(boolean available);
}
