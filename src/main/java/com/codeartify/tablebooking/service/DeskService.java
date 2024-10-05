package com.codeartify.tablebooking.service;

import com.codeartify.tablebooking.dto.ReservationRequest;
import com.codeartify.tablebooking.model.Desk;
import com.codeartify.tablebooking.repository.DeskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class DeskService {
    public final DeskRepository deskRepository;
    public final TeamDeskFinderService teamDeskFinderService;
    public final RandomService randomService;

    public Optional<Desk> findDesk(ReservationRequest request) {
        Optional<Desk> deskOpt;
        if (request.getDeskId() == null) {
            List<Desk> availDesks = this.deskRepository.findByAvailable(true);
            if (!availDesks.isEmpty()) {
                if (request.isSitCloseToTeam()) {
                    deskOpt = teamDeskFinderService.findDesk(request, availDesks);
                } else {
                    deskOpt = Optional.of(availDesks.get(randomService.nextRand(availDesks.size())));
                }
            } else {
                deskOpt = Optional.empty();
            }
        } else {
            deskOpt = this.deskRepository.findById(request.getDeskId());
        }
        return deskOpt;
    }
}
