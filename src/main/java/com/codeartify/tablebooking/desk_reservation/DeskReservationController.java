package com.codeartify.tablebooking.desk_reservation;

import com.codeartify.tablebooking.dto.ReservationRequest;
import com.codeartify.tablebooking.model.Desk;
import com.codeartify.tablebooking.model.Reservation;
import com.codeartify.tablebooking.repository.DeskRepository;
import com.codeartify.tablebooking.repository.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/desks")
public class DeskReservationController {
    private final DeskRepository deskRepository;
    private final ReservationRepository reservationRepository;
    private final Random random = new Random();

    public DeskReservationController(DeskRepository deskRepository, ReservationRepository reservationRepository) {
        this.deskRepository = deskRepository;
        this.reservationRepository = reservationRepository;
    }

    @PostMapping("/reserve")
    public ResponseEntity<Object> reserveDesk(@RequestBody ReservationRequest request) {
        Optional<Desk> deskOpt;
        var deskId = request.getDeskId();
        var teamMembers = request.getTeamMembers();
        var wantsToSitCloseToTeam = request.isSitCloseToTeam();

        deskOpt = findDeskFor(deskId, wantsToSitCloseToTeam, teamMembers);

        if (deskOpt.isPresent()) {
            List<Reservation> existingReservations = reservationRepository.findByReservedBy(request.getReservedBy());

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

        var desksTeam = findDesksOfTeam(teamMembers, availableDesks);

        if (!desksTeam.isEmpty()) {
            return Optional.of(desksTeam.get(random.nextInt(desksTeam.size())));
        } else {
            return Optional.of(availableDesks.get(random.nextInt(availableDesks.size())));
        }
    }

    private List<Desk> findDesksOfTeam(List<String> teamMembers, List<Desk> availableDesks) {
        var teamDeskIds = findTeamDeskIdsFor(teamMembers);

        return findTeamDesks(availableDesks, teamDeskIds);
    }

    private List<Long> findTeamDeskIdsFor(List<String> teamMembers) {
        return reservationRepository.findByReservedBy(teamMembers.stream().findFirst().orElse(null))
                .stream()
                .map(Reservation::getDeskId)
                .toList();
    }

    private static List<Desk> findTeamDesks(List<Desk> availableDesks, List<Long> teamDeskIds) {
        return availableDesks.stream()
                .filter(desk -> teamDeskIds.contains(desk.getId()))
                .toList();
    }
}

