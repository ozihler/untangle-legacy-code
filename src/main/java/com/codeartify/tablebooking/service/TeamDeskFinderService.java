package com.codeartify.tablebooking.service;

import com.codeartify.tablebooking.dto.ReservationRequest;
import com.codeartify.tablebooking.model.Desk;
import com.codeartify.tablebooking.model.Reservation;
import com.codeartify.tablebooking.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class TeamDeskFinderService {
    private final ReservationRepository reservationRepository;
    private final RandomService randomService;

    public Optional<Desk> findDesk(ReservationRequest request, List<Desk> availableDesks) {
        Optional<Desk> deskOpt;
        var teamDeskIds = this.reservationRepository.findByReservedBy(request.getTeamMembers().stream().findFirst().orElse(null))
                .stream()
                .map(Reservation::getDeskId)
                .toList();

        var desksTeam = availableDesks.stream()
                .filter(desk -> teamDeskIds.contains(desk.getId()))
                .toList();
        if (!desksTeam.isEmpty()) {
            deskOpt = Optional.of(desksTeam.get(randomService.nextRand(desksTeam.size())));
        } else {
            return Optional.of(availableDesks.get(randomService.nextRand(availableDesks.size())));
        }
        return deskOpt;
    }
}
