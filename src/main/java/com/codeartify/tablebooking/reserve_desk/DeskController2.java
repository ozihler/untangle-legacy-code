package com.codeartify.tablebooking.reserve_desk;

import com.codeartify.tablebooking.dto.ReservationRequest;
import com.codeartify.tablebooking.model.Desk;
import com.codeartify.tablebooking.model.Reservation;
import com.codeartify.tablebooking.repository.DeskRepository;
import com.codeartify.tablebooking.repository.ReservationRepository;
import com.codeartify.tablebooking.service.DeskService;
import com.codeartify.tablebooking.service.ReservationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class DeskController2 {
    private final DeskRepository deskRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final DeskService deskService;

    @PostMapping("/reserve")
    public ResponseEntity<Object> reserveDesk(@RequestBody ReservationRequest request) {
        var deskOpt = deskService.findDesk(request);

        if (deskOpt.isPresent()) {
            return reservationService.bookDesk(request, deskOpt);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

