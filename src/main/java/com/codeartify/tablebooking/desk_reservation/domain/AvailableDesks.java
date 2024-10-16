package com.codeartify.tablebooking.desk_reservation.domain;

import com.codeartify.tablebooking.model.Desk;

import java.util.List;

public record AvailableDesks(List<Desk> availableDesks) {
    public List<Desk> findTeamDesks(List<Long> teamDeskIds) {
        return availableDesks.stream()
                .filter(desk -> teamDeskIds.contains(desk.getId()))
                .toList();
    }
}
