package com.codeartify.tablebooking.service;

import com.codeartify.tablebooking.dto.ReservationRequest;
import com.codeartify.tablebooking.model.Desk;
import com.codeartify.tablebooking.model.Reservation;
import com.codeartify.tablebooking.repository.DeskRepository;
import com.codeartify.tablebooking.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class ReservationService {

    public final DeskRepository deskRepository;
    public final ReservationRepository reservationRepository;
    public final DeskReservationCheckerService deskReservationCheckerService;

    public ResponseEntity<Object> bookDesk(ReservationRequest request, Optional<Desk> deskOpt) {
        List<Reservation> existingReservations = this.reservationRepository.findByReservedBy(request.getReservedBy());
        var teamMembers = request.getTeamMembers();

        if (deskOpt.isEmpty()) {
            // can never happen under normal conditions
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
                if (request.getRole().equals("manager") || teamMembers == null || teamMembers.isEmpty()) {
                    if (request.getTypePreference() == null || request.getTypePreference().equals(desk.getType())) {
                        if (!request.isNearWindow() || desk.isNearWindow()) {
                            if (!request.isNeedsMonitor() || desk.isHasMonitor()) {
                                if (!request.isNeedsAdjustableDesk() || desk.isAdjustable()) {
                                    if (!desk.isReservedForManager() || request.getRole().equals("manager")) {
                                        if (!request.isRecurring() || (request.getRecurrencePattern() != null && !request.getRecurrencePattern().isEmpty())) {
                                            var deskReserved = deskReservationCheckerService.isDeskReserved(request, existingReservations, desk);

                                            if (deskReserved == null) {
                                                try {
                                                    reservation.setDeskId(desk.getId());

                                                    var memberHasReservedResponse = deskReservationCheckerService.hasMemberReserved(request, teamMembers);

                                                    if (memberHasReservedResponse == null) {
                                                        this.reservationRepository.save(reservation);
                                                        desk.setAvailable(false);
                                                        this.deskRepository.save(desk);
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
                                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This desk is reserved for managers only.");
                                    }
                                } else {
                                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Requested desk is not adjustable.");
                                }
                            } else {
                                return ResponseEntity.status(HttpStatus.CONFLICT).body("Requested desk does not have a monitor.");
                            }
                        } else {
                            return ResponseEntity.status(HttpStatus.CONFLICT).body("Requested desk is not near a window.");
                        }
                    } else {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("Requested desk type is not available.");
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Insufficient privileges to get desk");
                }
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Desk is not available");
            }
        }
    }

}
