package com.codeartify.tablebooking.reserve_desk.domain;

import com.codeartify.tablebooking.model.Desk;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public record AvailableDesks(List<Desk> availableDesks) {
    public boolean nonAvailable() {
        return availableDesks().isEmpty();
    }

    public Optional<Desk> getAnyAvailableDesk() {
        return Optional.of(availableDesks().get(new Random().nextInt(availableDesks().size())));
    }

    public List<Desk> getTeamDesks(List<Long> teamDeskIds) {
        return availableDesks().stream()
                .filter(desk -> teamDeskIds.contains(desk.getId()))
                .toList();
    }
}
