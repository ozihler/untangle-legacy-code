package com.codeartify.tablebooking.controller;

import com.codeartify.tablebooking.dto.ReservationRequest;
import com.codeartify.tablebooking.model.Desk;
import com.codeartify.tablebooking.repository.DeskRepository;
import com.codeartify.tablebooking.repository.ReservationRepository;
import com.codeartify.tablebooking.service.DeskReservationCheckerService;
import com.codeartify.tablebooking.service.DeskService;
import com.codeartify.tablebooking.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeskControllerApprovalTestShould {

    @Test
    void test() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        when(deskRepository.findByAvailable(false)).thenReturn(List.of());
        DeskService deskService = new DeskService(deskRepository, null, null);

        var deskController = new DeskController(null, null, null, deskService);

        ReservationRequest request = new ReservationRequest();

        var response = deskController.reserveDesk(request);

        assertEquals(ResponseEntity.notFound().build(), response);
    }

    @Test
    void test2() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        when(deskRepository.findByAvailable(false)).thenReturn(List.of());
        when(deskRepository.findById(1L)).thenReturn(Optional.of(new Desk()));

        DeskService deskService = new DeskService(deskRepository, null, null);

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        when(reservationRepository.findByReservedBy(any())).thenReturn(null);

        ReservationService reservationService = new ReservationService(null, reservationRepository, null);
        var deskController = new DeskController(null, null, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setDeskId(1L);

        var response = deskController.reserveDesk(request);

        assertEquals(ResponseEntity.status(HttpStatus.CONFLICT).body("Desk is not available"), response);
    }

    @Test
    void test3() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        when(deskRepository.findByAvailable(false)).thenReturn(List.of());
        var desk = new Desk();
        desk.setAvailable(true);
        when(deskRepository.findById(1L)).thenReturn(Optional.of(desk));

        DeskService deskService = new DeskService(deskRepository, null, null);

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        when(reservationRepository.findByReservedBy(any())).thenReturn(List.of());

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(null);
        ReservationService reservationService = new ReservationService(null, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(null, null, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setDeskId(1L);
        request.setRole("manager");

        var response = deskController.reserveDesk(request);

        assertEquals(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while saving the reservation: Cannot invoke \"com.codeartify.tablebooking.repository.DeskRepository.save(Object)\" because \"this.deskRepository\" is null"), response);
    }
}
