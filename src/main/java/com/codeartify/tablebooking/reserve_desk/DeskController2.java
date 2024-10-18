package com.codeartify.tablebooking.reserve_desk;

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
public class DeskController2 {
    private final DeskRepository deskRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final DeskService deskService;
    private final Random random = new Random();

    @PostMapping("/reserve")
    public ResponseEntity<Object> reserveDesk(@RequestBody ReservationRequest request) {
        Optional<Desk> deskOpt1;
        if (request.getDeskId() == null) {
            List<Desk> availDesks = deskRepository.findByAvailable(true);
            if (!availDesks.isEmpty()) {
                if (request.isSitCloseToTeam()) {
                    Optional<Desk> result;
                    Optional<Desk> deskOpt;
                    var teamDeskIds = reservationRepository.findByReservedBy(request.getTeamMembers().stream().findFirst().orElse(null))
                            .stream()
                            .map(Reservation::getDeskId)
                            .toList();

                    var desksTeam = availDesks.stream()
                            .filter(desk -> teamDeskIds.contains(desk.getId()))
                            .toList();
                    if (!desksTeam.isEmpty()) {
                        deskOpt = Optional.of(desksTeam.get(random.nextInt(desksTeam.size())));
                        result = deskOpt;
                    } else {
                        result = Optional.of(availDesks.get(random.nextInt(availDesks.size())));
                    }
                    deskOpt1 = result;
                } else {
                    deskOpt1 = Optional.of(availDesks.get(random.nextInt(availDesks.size())));
                }
            } else {
                deskOpt1 = Optional.empty();
            }
        } else {
            deskOpt1 = deskRepository.findById(request.getDeskId());
        }
        var deskOpt = deskOpt1;

        if (deskOpt.isPresent()) {
            List<Reservation> existingReservations = reservationRepository.findByReservedBy(request.getReservedBy());
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
                                            reservationRepository.save(reservation);
                                            desk.setAvailable(false);
                                            deskRepository.save(desk);
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

}

