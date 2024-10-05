package com.codeartify.tablebooking.service;

import com.codeartify.tablebooking.controller.ReservationRequest;
import com.codeartify.tablebooking.entity.Desk;
import com.codeartify.tablebooking.entity.Reservation;
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

    private final DeskRepository deskRepository;
    private final ReservationRepository reservationRepository;
    private final DeskReservationCheckerService deskReservationCheckerService;

    public ResponseEntity<Object> bookDesk(ReservationRequest request, Optional<Desk> deskOpt) {
        Desk desk = deskOpt.get();
        List<Reservation> existingReservations = this.reservationRepository.findByReservedBy(request.getReservedBy());
        var teamMembers = request.getTeamMembers();

        Reservation reservation = new Reservation();
        reservation.setReservedBy(request.getReservedBy());
        reservation.setReservationType(request.getReservationType());
        reservation.setTeamMembers(teamMembers);
        reservation.setRecurring(request.isRecurring());
        reservation.setRecurrencePattern(request.getRecurrencePattern());
        reservation.setPurpose(request.getPurpose());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());

        if (!desk.isAvailable()) {
            return ResponseEntity.notFound().build();
        }
        if (!request.getRole().equals("manager") && teamMembers != null && !teamMembers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Insufficient privileges to get desk");
        }
        if (request.getTypePreference() != null && !request.getTypePreference().equals(desk.getType())) {
            return ResponseEntity.badRequest().body("Requested desk type is not available.");
        }
        if (request.isNearWindow() && !desk.isNearWindow()) {
            return ResponseEntity.badRequest().body("Requested desk is not near a window.");
        }
        if (request.isNeedsMonitor() && !desk.isHasMonitor()) {
            return ResponseEntity.badRequest().body("Requested desk does not have a monitor.");
        }
        if (request.isNeedsAdjustableDesk() && !desk.isAdjustable()) {
            return ResponseEntity.badRequest().body("Requested desk is not adjustable.");
        }
        if (desk.isReservedForManager() && !request.getRole().equals("manager")) {
            return ResponseEntity.badRequest().body("This desk is reserved for managers only.");
        }
        if (request.isRecurring() && (request.getRecurrencePattern() == null || request.getRecurrencePattern().isEmpty())) {
            return ResponseEntity.badRequest().body("Recurring reservations must have a recurrence pattern.");
        }
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
                return ResponseEntity.badRequest().body("An error occurred while saving the reservation: " + e.getMessage());
            }
        } else {
            return deskReserved;
        }
        return ResponseEntity.ok(desk);
    }

}

