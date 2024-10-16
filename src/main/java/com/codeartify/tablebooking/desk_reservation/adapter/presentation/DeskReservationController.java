package com.codeartify.tablebooking.desk_reservation.adapter.presentation;

import com.codeartify.tablebooking.desk_reservation.adapter.data_access.ReservationRepository;
import com.codeartify.tablebooking.desk_reservation.domain.AvailableDesks;
import com.codeartify.tablebooking.desk_reservation.domain.Team;
import com.codeartify.tablebooking.dto.ReservationRequest;
import com.codeartify.tablebooking.model.Desk;
import com.codeartify.tablebooking.model.Reservation;
import com.codeartify.tablebooking.repository.DeskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/desks")
public class DeskReservationController {
    private final DeskRepository deskRepository;
    private final com.codeartify.tablebooking.repository.ReservationRepository reservationRepository;
    private final Random random = new Random();
    private final ReservationRepository reservationRepository1;

    public DeskReservationController(DeskRepository deskRepository, com.codeartify.tablebooking.repository.ReservationRepository reservationRepository, ReservationRepository reservationRepository1) {
        this.deskRepository = deskRepository;
        this.reservationRepository = reservationRepository;
        this.reservationRepository1 = reservationRepository1;
    }

    @PostMapping("/reserve")
    public ResponseEntity<Object> reserveDesk(@RequestBody ReservationRequest request) {
        Optional<Desk> deskOpt;
        var deskId = request.getDeskId();
        var teamMembers = request.getTeamMembers();
        var wantsToSitCloseToTeam = request.isSitCloseToTeam();

        deskOpt = findDeskFor(deskId, wantsToSitCloseToTeam, teamMembers);

        if (deskOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Reservation> existingReservations = reservationRepository.findByReservedBy(request.getReservedBy());

        Desk desk = deskOpt.get();


        if (!desk.isAvailable()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Desk is not available");
        }
        if (request.isNeedsMonitor() && !desk.isHasMonitor()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Requested desk does not have a monitor.");
        }
        if (request.isNeedsAdjustableDesk() && !desk.isAdjustable()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Requested desk is not adjustable.");
        }
        if (request.isRecurring() && (request.getRecurrencePattern() == null || request.getRecurrencePattern().isEmpty())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Recurring reservations must have a recurrence pattern.");
        }
        ResponseEntity<Object> deskReserved = null;
        for (Reservation existingReservation : existingReservations) {
            if (existingReservation.getDeskId().equals(desk.getId()) &&
                    existingReservation.getStartTime().equals(request.getStartTime()) &&
                    existingReservation.getEndTime().equals(request.getEndTime())) {
                deskReserved = ResponseEntity.status(HttpStatus.CONFLICT).body("There is already a reservation for the selected time.");
                break;
            }
        }

        if (deskReserved != null) {
            return deskReserved;
        }

        Optional<String> conflictingMember = findConflictWithOtherMember(teamMembers, request.getReservedBy(), request.getStartTime(), request.getEndTime());


        if (conflictingMember.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Team member " + conflictingMember.get() + " already has a reservation during the selected time.");
        }

        try {

            Reservation reservation = new Reservation();
            reservation.setReservedBy(request.getReservedBy());
            reservation.setReservationType(request.getReservationType());
            reservation.setTeamMembers(teamMembers);
            reservation.setRecurring(request.isRecurring());
            reservation.setRecurrencePattern(request.getRecurrencePattern());
            reservation.setPurpose(request.getPurpose());
            reservation.setStartTime(request.getStartTime());
            reservation.setEndTime(request.getEndTime());
            reservation.setDeskId(desk.getId());
            reservationRepository.save(reservation);

            desk.setAvailable(false);
            deskRepository.save(desk);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while saving the reservation: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(desk);

    }


    private Optional<String> findConflictWithOtherMember(List<String> teamMembers, String reservedBy, String startTime, String endTime) {
        if (teamMembers == null) {
            return Optional.empty();
        }
        return teamMembers.stream()
                .filter(member -> !member.equals(reservedBy))
                .filter(member -> reservationRepository.findByReservedBy(member).stream().anyMatch(existingReservation -> matchingTimes(startTime, endTime, existingReservation))).findFirst();
    }

    private static boolean matchingTimes(String startTime, String endTime, Reservation existingReservation) {
        return existingReservation.getStartTime().equals(startTime) &&
                existingReservation.getEndTime().equals(endTime);
    }

    private Optional<Desk> findDeskFor(Long deskId, boolean wantsToSitCloseToTeam, List<String> teamMembers) {
        if (deskId != null) {
            return deskRepository.findById(deskId);
        }
        List<Desk> availableDesks = deskRepository.findByAvailable(true);
        if (availableDesks.isEmpty()) {
            return Optional.empty();
        }

        if (!wantsToSitCloseToTeam) {
            return Optional.of(availableDesks.get(random.nextInt(availableDesks.size())));
        }

        var team = new Team(teamMembers);
        var availableDesks1 = new AvailableDesks(availableDesks);

        var desksOfTeam = reservationRepository1.findDesksOfTeam(team, availableDesks1);

        if (!desksOfTeam.isEmpty()) {
            return Optional.of(desksOfTeam.get(random.nextInt(desksOfTeam.size())));
        } else {
            return Optional.of(availableDesks.get(random.nextInt(availableDesks.size())));
        }
    }

}

