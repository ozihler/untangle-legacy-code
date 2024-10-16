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

@AllArgsConstructor
@RestController
@RequestMapping("/api/desks")
public class DeskController {
    private final DeskRepository deskRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final DeskService deskService;

    @PostMapping("/reserve")
    public ResponseEntity<Object> reserveDesk(@RequestBody ReservationRequest request) {
        Optional<Desk> deskOpt1;
        if (request.getDeskId() == null) {
            List<Desk> availDesks = deskService.deskRepository.findByAvailable(true);
            if (!availDesks.isEmpty()) {
                if (request.isSitCloseToTeam()) {
                    Optional<Desk> result;
                    Optional<Desk> deskOpt;
                    var teamDeskIds = deskService.teamDeskFinderService.reservationRepository.findByReservedBy(request.getTeamMembers().stream().findFirst().orElse(null))
                            .stream()
                            .map(Reservation::getDeskId)
                            .toList();

                    var desksTeam = availDesks.stream()
                            .filter(desk -> teamDeskIds.contains(desk.getId()))
                            .toList();
                    if (!desksTeam.isEmpty()) {
                        deskOpt = Optional.of(desksTeam.get(deskService.teamDeskFinderService.randomService.random.nextInt(desksTeam.size())));
                        result = deskOpt;
                    } else {
                        result = Optional.of(availDesks.get(deskService.teamDeskFinderService.randomService.random.nextInt(availDesks.size())));
                    }
                    deskOpt1 = result;
                } else {
                    deskOpt1 = Optional.of(availDesks.get(deskService.randomService.random.nextInt(availDesks.size())));
                }
            } else {
                deskOpt1 = Optional.empty();
            }
        } else {
            deskOpt1 = deskService.deskRepository.findById(request.getDeskId());
        }
        var deskOpt = deskOpt1;

        if (deskOpt.isPresent()) {
            List<Reservation> existingReservations = reservationService.reservationRepository.findByReservedBy(request.getReservedBy());
            var teamMembers = request.getTeamMembers();

            if (deskOpt.isEmpty()) {
                // not reachable from controller as deskOpt would already return not found inside controller
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Desk not found");
            } else {
                Desk desk = deskOpt.get();
                Reservation reservation = new Reservation();
                reservation.setReservedBy(request.getReservedBy());
                reservation.setReservationType(request.getReservationType());
                reservation.setTeamMembers(teamMembers);
                reservation.setRecurring(request.isRecurring());
                reservation.setRecurrencePattern(request.getRecurrencePattern());
                reservation.setPurpose(request.getPurpose());
                reservation.setStartTime(request.getStartTime());
                reservation.setEndTime(request.getEndTime());

                if (desk.isAvailable()) {
                    if (!request.isNeedsMonitor() || desk.isHasMonitor()) {
                        if (!request.isNeedsAdjustableDesk() || desk.isAdjustable()) {
                            if (!request.isRecurring() || (request.getRecurrencePattern() != null && !request.getRecurrencePattern().isEmpty())) {
                                ResponseEntity<Object> deskReserved = null;
                                for (Reservation existingReservation : existingReservations) {
                                    if (existingReservation.getDeskId().equals(desk.getId()) &&
                                            existingReservation.getStartTime().equals(request.getStartTime()) &&
                                            existingReservation.getEndTime().equals(request.getEndTime())) {
                                        deskReserved = ResponseEntity.status(HttpStatus.CONFLICT).body("There is already a reservation for the selected time.");
                                        break;
                                    }
                                }
                                if (deskReserved == null) {
                                    try {
                                        reservation.setDeskId(desk.getId());

                                        ResponseEntity<Object> memberHasReservedResponse = null;
                                        boolean finished = false;
                                        if (teamMembers != null) {
                                            for (String member : teamMembers) {
                                                if (!member.equals(request.getReservedBy())) {
                                                    for (Reservation existingReservation : reservationService.deskReservationCheckerService.reservationRepository.findByReservedBy(member)) {
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
                                            reservationService.reservationRepository.save(reservation);
                                            desk.setAvailable(false);
                                            reservationService.deskRepository.save(desk);
                                        } else {
                                            return memberHasReservedResponse;
                                        }

                                    } catch (Exception e) {
                                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while saving the reservation: " + e.getMessage());
                                    }
                                } else {
                                    return deskReserved;
                                }
                                return ResponseEntity.status(HttpStatus.CREATED).body(desk);
                            } else {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Recurring reservations must have a recurrence pattern.");
                            }

                        } else {
                            return ResponseEntity.status(HttpStatus.CONFLICT).body("Requested desk is not adjustable.");
                        }
                    } else {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("Requested desk does not have a monitor.");
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Desk is not available");
                }
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }


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

