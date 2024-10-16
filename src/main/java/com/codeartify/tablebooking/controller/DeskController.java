package com.codeartify.tablebooking.controller;

import com.codeartify.tablebooking.dto.ReservationRequest;
import com.codeartify.tablebooking.model.Desk;
import com.codeartify.tablebooking.model.Reservation;
import com.codeartify.tablebooking.repository.DeskRepository;
import com.codeartify.tablebooking.repository.ReservationRepository;
import com.codeartify.tablebooking.service.DeskService;
import com.codeartify.tablebooking.service.ReservationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@AllArgsConstructor
@RestController
@RequestMapping("/api/desks")
public class DeskController {
    private final DeskRepository deskRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final DeskService deskService;

    @PostMapping("/reserve/team")
    public String reserveForTeam(@RequestBody ReservationRequest request) {

        List<Desk> availableDesks = deskRepository.findByAvailable(true);
        if (availableDesks.size() < request.getTeamMembers().size()) {
            return "Not enough desks available for the entire team.";
        } else {
            try {
                for (String teamMember : request.getTeamMembers()) {
                    Desk desk = availableDesks.removeFirst();
                    Reservation reservation = new Reservation();
                    reservation.setDeskId(desk.getId());
                    reservation.setReservedBy(teamMember);
                    reservation.setReservationType(request.getReservationType());
                    reservation.setPurpose(request.getPurpose());
                    reservationRepository.save(reservation);
                    desk.setAvailable(false);
                    deskRepository.save(desk);
                }
            } catch (Exception e) {
                return "An error occurred while saving team reservations: " + e.getMessage();
            }
        }

        return "Team reservation successful";
    }

    @DeleteMapping("/cancel/{reservationId}")
    public String cancelReservation(@PathVariable Long reservationId) {
        try {
            Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
            if (reservationOpt.isPresent()) {
                Reservation reservation = reservationOpt.get();
                Optional<Desk> deskOpt = deskRepository.findById(reservation.getDeskId());
                if (deskOpt.isPresent()) {
                    Desk desk = deskOpt.get();
                    desk.setAvailable(true);
                    deskRepository.save(desk);
                }
                reservationRepository.deleteById(reservationId);
                return "Reservation canceled successfully";
            }
        } catch (Exception e) {
            return "An error occurred while canceling the reservation: " + e.getMessage();
        }
        return "Reservation not found";
    }
}

