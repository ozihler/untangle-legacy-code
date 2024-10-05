package com.codeartify.tablebooking.service;

import com.codeartify.tablebooking.dto.ReservationRequest;
import com.codeartify.tablebooking.model.Desk;
import com.codeartify.tablebooking.model.Reservation;
import com.codeartify.tablebooking.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DeskReservationCheckerService {
    public ReservationRepository reservationRepository;

    public ResponseEntity<Object> isDeskReserved(ReservationRequest request, List<Reservation> existingReservations, Desk desk) {
        for (Reservation existingReservation : existingReservations) {
            if (existingReservation.getDeskId().equals(desk.getId()) &&
                    existingReservation.getStartTime().equals(request.getStartTime()) &&
                    existingReservation.getEndTime().equals(request.getEndTime())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("There is already a reservation for the selected time.");
            }
        }
        return null;
    }

    public ResponseEntity<Object> hasMemberReserved(ReservationRequest request, List<String> teamMembers) {
        if (teamMembers != null) {
            for (String member : teamMembers) {
                if (!member.equals(request.getReservedBy())) {
                    for (Reservation existingReservation : this.reservationRepository.findByReservedBy(member)) {
                        if (existingReservation.getStartTime().equals(request.getStartTime()) &&
                                existingReservation.getEndTime().equals(request.getEndTime())) {
                            return ResponseEntity.status(HttpStatus.CONFLICT).body("Team member " + member + " already has a reservation during the selected time.");
                        }
                    }
                }
            }
        }
        return null;
    }
}
