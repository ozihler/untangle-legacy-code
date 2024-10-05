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

import java.util.ArrayList;
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

    @Test
    void test4() {
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
        request.setRole("user");
        request.setTeamMembers(new ArrayList<>());

        var response = deskController.reserveDesk(request);

        assertEquals(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while saving the reservation: Cannot invoke \"com.codeartify.tablebooking.repository.DeskRepository.save(Object)\" because \"this.deskRepository\" is null"), response);
    }

    @Test
    void test5() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        when(deskRepository.findByAvailable(false)).thenReturn(List.of());
        var desk = new Desk();
        desk.setAvailable(true);
        desk.setType("another-desk-type");

        when(deskRepository.findById(1L)).thenReturn(Optional.of(desk));

        DeskService deskService = new DeskService(deskRepository, null, null);

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        when(reservationRepository.findByReservedBy(any())).thenReturn(List.of());

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(null);
        ReservationService reservationService = new ReservationService(null, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(null, null, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setDeskId(1L);
        request.setRole("user");
        request.setTeamMembers(new ArrayList<>());
        request.setTypePreference("desk-type");

        var response = deskController.reserveDesk(request);

        assertEquals(ResponseEntity.status(HttpStatus.CONFLICT).body("Requested desk type is not available."), response);
    }

    @Test
    void test6() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        when(deskRepository.findByAvailable(false)).thenReturn(List.of());
        var desk = new Desk();
        desk.setAvailable(true);
        desk.setType("desk-type");
        desk.setNearWindow(false);

        when(deskRepository.findById(1L)).thenReturn(Optional.of(desk));

        DeskService deskService = new DeskService(deskRepository, null, null);

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        when(reservationRepository.findByReservedBy(any())).thenReturn(List.of());

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(null);
        ReservationService reservationService = new ReservationService(null, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(null, null, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setDeskId(1L);
        request.setRole("user");
        request.setTeamMembers(new ArrayList<>());
        request.setTypePreference("desk-type");
        request.setNearWindow(true);

        var response = deskController.reserveDesk(request);

        assertEquals(ResponseEntity.status(HttpStatus.CONFLICT).body("Requested desk is not near a window."), response);
    }

    @Test
    void test7() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        when(deskRepository.findByAvailable(false)).thenReturn(List.of());
        var desk = new Desk();
        desk.setAvailable(true);
        desk.setType("desk-type");
        desk.setNearWindow(true);
        desk.setHasMonitor(false);

        when(deskRepository.findById(1L)).thenReturn(Optional.of(desk));

        DeskService deskService = new DeskService(deskRepository, null, null);

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        when(reservationRepository.findByReservedBy(any())).thenReturn(List.of());

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(null);
        ReservationService reservationService = new ReservationService(null, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(null, null, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setDeskId(1L);
        request.setRole("user");
        request.setTeamMembers(new ArrayList<>());
        request.setTypePreference("desk-type");
        request.setNearWindow(true);
        request.setNeedsMonitor(true);

        var response = deskController.reserveDesk(request);

        assertEquals(ResponseEntity.status(HttpStatus.CONFLICT).body("Requested desk does not have a monitor."), response);
    }

    @Test
    void test8() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        when(deskRepository.findByAvailable(false)).thenReturn(List.of());
        var desk = new Desk();
        desk.setAvailable(true);
        desk.setType("desk-type");
        desk.setNearWindow(true);
        desk.setHasMonitor(true);
        desk.setAdjustable(false);

        when(deskRepository.findById(1L)).thenReturn(Optional.of(desk));

        DeskService deskService = new DeskService(deskRepository, null, null);

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        when(reservationRepository.findByReservedBy(any())).thenReturn(List.of());

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(null);
        ReservationService reservationService = new ReservationService(null, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(null, null, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setDeskId(1L);
        request.setRole("user");
        request.setTeamMembers(new ArrayList<>());
        request.setTypePreference("desk-type");
        request.setNearWindow(true);
        request.setNeedsMonitor(true);
        request.setNeedsAdjustableDesk(true);

        var response = deskController.reserveDesk(request);

        assertEquals(ResponseEntity.status(HttpStatus.CONFLICT).body("Requested desk is not adjustable."), response);
    }

    @Test
    void test9() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        when(deskRepository.findByAvailable(false)).thenReturn(List.of());
        var desk = new Desk();
        desk.setAvailable(true);
        desk.setType("desk-type");
        desk.setNearWindow(true);
        desk.setHasMonitor(true);
        desk.setAdjustable(true);
        desk.setReservedForManager(true);

        when(deskRepository.findById(1L)).thenReturn(Optional.of(desk));

        DeskService deskService = new DeskService(deskRepository, null, null);

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        when(reservationRepository.findByReservedBy(any())).thenReturn(List.of());

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(null);
        ReservationService reservationService = new ReservationService(null, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(null, null, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setDeskId(1L);
        request.setRole("user");
        request.setTeamMembers(new ArrayList<>());
        request.setTypePreference("desk-type");
        request.setNearWindow(true);
        request.setNeedsMonitor(true);
        request.setNeedsAdjustableDesk(true);

        var response = deskController.reserveDesk(request);

        assertEquals(ResponseEntity.status(HttpStatus.FORBIDDEN).body("This desk is reserved for managers only."), response);
    }

    @Test
    void test10() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        when(deskRepository.findByAvailable(false)).thenReturn(List.of());
        var desk = new Desk();
        desk.setAvailable(true);
        desk.setType("desk-type");
        desk.setNearWindow(true);
        desk.setHasMonitor(true);
        desk.setAdjustable(true);
        desk.setReservedForManager(false);

        when(deskRepository.findById(1L)).thenReturn(Optional.of(desk));

        DeskService deskService = new DeskService(deskRepository, null, null);

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        when(reservationRepository.findByReservedBy(any())).thenReturn(List.of());

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(null);
        ReservationService reservationService = new ReservationService(null, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(null, null, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setDeskId(1L);
        request.setRole("user");
        request.setTeamMembers(new ArrayList<>());
        request.setTypePreference("desk-type");
        request.setNearWindow(true);
        request.setNeedsMonitor(true);
        request.setNeedsAdjustableDesk(true);
        request.setRecurring(true);

        var response = deskController.reserveDesk(request);

        assertEquals(ResponseEntity.badRequest().body("Recurring reservations must have a recurrence pattern."), response);
    }
}
