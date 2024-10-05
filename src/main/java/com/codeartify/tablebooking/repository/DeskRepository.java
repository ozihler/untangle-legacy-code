package com.codeartify.tablebooking.repository;

import com.codeartify.tablebooking.entity.Desk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeskRepository extends JpaRepository<Desk, Long> {
    List<Desk> findByAvailable(boolean available);

    List<Desk> findByTypeAndAvailable(String type, boolean available);

    List<Desk> findByLocationAndAvailable(String location, boolean available);

    List<Desk> findByNearWindowAndAvailable(boolean nearWindow, boolean available);

    List<Desk> findByHasMonitorAndAvailable(boolean hasMonitor, boolean available);

    List<Desk> findByIsAdjustableAndAvailable(boolean isAdjustable, boolean available);

    List<Desk> findByReservedForManagerAndAvailable(boolean reservedForManager, boolean available);
}
