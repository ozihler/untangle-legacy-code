package com.codeartify.tablebooking.reserve_desk;

import com.codeartify.tablebooking.dto.ReservationRequest;
import com.codeartify.tablebooking.model.Desk;
import com.codeartify.tablebooking.model.Reservation;
import com.codeartify.tablebooking.repository.DeskRepository;
import com.codeartify.tablebooking.repository.ReservationRepository;
import com.codeartify.tablebooking.reserve_desk.domain.AvailableDesks;
import com.codeartify.tablebooking.service.DeskService;
import com.codeartify.tablebooking.service.ReservationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@AllArgsConstructor
public class DeskController2 {
    private final DeskRepository deskRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final DeskService deskService;
    private final Random random = new Random();

    @PostMapping("/reserve")
    public ResponseEntity<Object> reserveDesk(@RequestBody ReservationRequest request) {
        var deskOpt = findDesk(request);

        if (deskOpt.isPresent()) {
            List<Reservation> existingReservations = reservationRepository.findByReservedBy(request.getReservedBy());
            var teamMembers = request.getTeamMembers();

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
            try {

                ResponseEntity<Object> memberHasReservedResponse = null;
                boolean finished = false;
                if (teamMembers != null) {
                    for (String member : teamMembers) {
                        if (!member.equals(request.getReservedBy())) {
                            for (Reservation existingReservation : reservationRepository.findByReservedBy(member)) {
                                if (existingReservation.getStartTime().equals(request.getStartTime()) &&
                                        existingReservation.getEndTime().equals(request.getEndTime())) {
                                    memberHasReservedResponse = ResponseEntity.status(HttpStatus.CONFLICT).body("Team member " + member + " already has a reservation during the selected time.");
                                    finished = true;
                                    break;
                                }
                            }
                            if (finished) break;
                        }
                    }
                }

                if (memberHasReservedResponse == null) {
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
                } else {
                    return memberHasReservedResponse;
                }

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while saving the reservation: " + e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(desk);

        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private Optional<Desk> findDesk(ReservationRequest request) {
        if (request.getDeskId() != null) {
            return deskRepository.findById(request.getDeskId());
        }

        List<Desk> availDesks = deskRepository.findByAvailable(true);

        var availableDesks = new AvailableDesks(availDesks);
        if (availableDesks.nonAvailable()) {
            return Optional.empty();
        }

        if (!request.isSitCloseToTeam()) {
            return availableDesks.getAnyAvailableDesk();
        }

        var teamDeskIds = reservationRepository.findByReservedBy(request.getTeamMembers().stream().findFirst().orElse(null))
                .stream()
                .map(Reservation::getDeskId)
                .toList();

        var desksTeam = availableDesks.getTeamDesks(teamDeskIds);
        if (!desksTeam.isEmpty()) {
            return Optional.of(desksTeam.get(random.nextInt(desksTeam.size())));
        } else {
            return availableDesks.getAnyAvailableDesk();
        }
    }


}

