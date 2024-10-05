package com.codeartify.tablebooking.controller;

import com.codeartify.tablebooking.dto.ReservationRequest;
import com.codeartify.tablebooking.model.Desk;
import com.codeartify.tablebooking.model.Reservation;
import com.codeartify.tablebooking.repository.DeskRepository;
import com.codeartify.tablebooking.repository.ReservationRepository;
import com.codeartify.tablebooking.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;

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

        var deskController = new DeskController(deskRepository, null, null, deskService);

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

        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, null);
        var deskController = new DeskController(deskRepository, reservationRepository, reservationService, deskService);

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
        when(deskRepository.save(any())).thenThrow(HttpServerErrorException.InternalServerError.class);

        DeskService deskService = new DeskService(deskRepository, null, null);

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        when(reservationRepository.findByReservedBy(any())).thenReturn(List.of());

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(deskRepository, reservationRepository, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setDeskId(1L);
        request.setRole("manager");

        var response = deskController.reserveDesk(request);

        assertEquals(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while saving the reservation: null"), response);
    }

    @Test
    void test16() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        when(deskRepository.findByAvailable(false)).thenReturn(List.of());
        var desk = new Desk();
        desk.setAvailable(true);
        when(deskRepository.findById(1L)).thenReturn(Optional.of(desk));
        when(deskRepository.save(any())).thenThrow(HttpServerErrorException.InternalServerError.class);

        DeskService deskService = new DeskService(deskRepository, null, null);

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        when(reservationRepository.findByReservedBy(any())).thenReturn(List.of());

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(deskRepository, reservationRepository, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setDeskId(1L);
        request.setTeamMembers(List.of("member1"));
        request.setRole("user");

        var response = deskController.reserveDesk(request);

        assertEquals("<403 FORBIDDEN Forbidden,Insufficient privileges to get desk,[]>", response.toString());
    }

    @Test
    void test4() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        when(deskRepository.findByAvailable(false)).thenReturn(List.of());
        var desk = new Desk();
        desk.setAvailable(true);
        when(deskRepository.findById(1L)).thenReturn(Optional.of(desk));
        when(deskRepository.save(any())).thenThrow(HttpServerErrorException.InternalServerError.class);

        DeskService deskService = new DeskService(deskRepository, null, null);

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        when(reservationRepository.findByReservedBy(any())).thenReturn(List.of());

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(deskRepository, reservationRepository, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setDeskId(1L);
        request.setRole("user");
        request.setTeamMembers(new ArrayList<>());

        var response = deskController.reserveDesk(request);

        assertEquals(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while saving the reservation: null"), response);
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

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(deskRepository, reservationRepository, reservationService, deskService);

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

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(deskRepository, reservationRepository, reservationService, deskService);

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

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(deskRepository, reservationRepository, reservationService, deskService);

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

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(deskRepository, reservationRepository, reservationService, deskService);

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

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(deskRepository, reservationRepository, reservationService, deskService);

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

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(deskRepository, reservationRepository, reservationService, deskService);

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

    @Test
    void test11() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        when(deskRepository.findByAvailable(false)).thenReturn(List.of());
        var desk = new Desk();
        desk.setId(2L);
        desk.setAvailable(true);
        desk.setType("desk-type");
        desk.setNearWindow(true);
        desk.setHasMonitor(true);
        desk.setAdjustable(true);
        desk.setReservedForManager(false);

        when(deskRepository.findById(1L)).thenReturn(Optional.of(desk));

        DeskService deskService = new DeskService(deskRepository, null, null);

        ReservationRepository reservationRepository = mock(ReservationRepository.class);

        var reservation = new Reservation();
        reservation.setDeskId(2L);
        reservation.setStartTime("08:00:00");
        reservation.setEndTime("16:30:00");

        when(reservationRepository.findByReservedBy(any())).thenReturn(List.of(reservation));

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(deskRepository, reservationRepository, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setDeskId(1L);
        request.setRole("user");
        request.setTeamMembers(new ArrayList<>());
        request.setTypePreference("desk-type");
        request.setNearWindow(true);
        request.setNeedsMonitor(true);
        request.setNeedsAdjustableDesk(true);
        request.setRecurring(false);
        request.setStartTime("08:00:00");
        request.setEndTime("16:30:00");

        var response = deskController.reserveDesk(request);

        assertEquals(ResponseEntity.status(HttpStatus.CONFLICT).body("There is already a reservation for the selected time."), response);
    }

    @Test
    void test12() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        when(deskRepository.findByAvailable(false)).thenReturn(List.of());
        var desk = new Desk();
        desk.setId(2L);
        desk.setAvailable(true);
        desk.setType("desk-type");
        desk.setNearWindow(true);
        desk.setHasMonitor(true);
        desk.setAdjustable(true);
        desk.setReservedForManager(false);

        when(deskRepository.findById(1L)).thenReturn(Optional.of(desk));

        DeskService deskService = new DeskService(deskRepository, null, null);

        ReservationRepository reservationRepository = mock(ReservationRepository.class);

        var reservation = new Reservation();
        reservation.setDeskId(1L);
        reservation.setStartTime("08:00:00");
        reservation.setEndTime("16:30:00");

        when(reservationRepository.findByReservedBy(any())).thenReturn(List.of(reservation));

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(deskRepository, reservationRepository, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setDeskId(1L);
        request.setRole("user");
        request.setTeamMembers(new ArrayList<>());
        request.setTypePreference("desk-type");
        request.setNearWindow(true);
        request.setNeedsMonitor(true);
        request.setNeedsAdjustableDesk(true);
        request.setRecurring(false);
        request.setStartTime("08:00:00");
        request.setEndTime("16:30:00");

        var response = deskController.reserveDesk(request);

        assertEquals("<201 CREATED Created,Desk(id=2, type=desk-type, available=false, location=null, nearWindow=true, hasMonitor=true, isAdjustable=true, reservedForManager=false),[]>", response.toString());
    }

    @Test
    void test13() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        when(deskRepository.findByAvailable(false)).thenReturn(List.of());
        var desk = new Desk();
        desk.setId(2L);
        desk.setAvailable(true);
        desk.setType("desk-type");
        desk.setNearWindow(true);
        desk.setHasMonitor(true);
        desk.setAdjustable(true);
        desk.setReservedForManager(false);

        when(deskRepository.findById(1L)).thenReturn(Optional.of(desk));

        DeskService deskService = new DeskService(deskRepository, null, null);

        ReservationRepository reservationRepository = mock(ReservationRepository.class);

        var reservation = new Reservation();
        reservation.setDeskId(1L);
        reservation.setStartTime("08:00:00");
        reservation.setEndTime("16:30:00");
        reservation.setReservedBy("member2");

        when(reservationRepository.findByReservedBy("member2")).thenReturn(List.of(reservation));

        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(deskRepository, reservationRepository, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setDeskId(1L);
        request.setRole("manager");
        request.setTeamMembers(List.of("member2"));
        request.setReservedBy("member1");
        request.setTypePreference("desk-type");
        request.setNearWindow(true);
        request.setNeedsMonitor(true);
        request.setNeedsAdjustableDesk(true);
        request.setRecurring(false);
        request.setStartTime("08:00:00");
        request.setEndTime("16:30:00");

        var response = deskController.reserveDesk(request);

        assertEquals("<409 CONFLICT Conflict,Team member member2 already has a reservation during the selected time.,[]>", response.toString());
    }

    @Test
    void test14() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        var desk = new Desk();
        desk.setId(1L);
        when(deskRepository.findByAvailable(true)).thenReturn(List.of(desk));

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        var reservation = new Reservation();
        reservation.setDeskId(desk.getId());
        when(reservationRepository.findByReservedBy(any())).thenReturn(List.of(reservation));

        RandomService randomService = new RandomService();
        TeamDeskFinderService teamDeskFinderService = new TeamDeskFinderService(reservationRepository, randomService);
        DeskService deskService = new DeskService(deskRepository, teamDeskFinderService, randomService);


        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(deskRepository, reservationRepository, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setSitCloseToTeam(true);
        request.setTeamMembers(List.of("member1"));

        var response = deskController.reserveDesk(request);

        assertEquals("<409 CONFLICT Conflict,Desk is not available,[]>", response.toString());
    }

    @Test
    void test15() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        var desk = new Desk();
        desk.setId(1L);
        when(deskRepository.findByAvailable(true)).thenReturn(List.of(desk));

        ReservationRepository reservationRepository = mock(ReservationRepository.class);

        when(reservationRepository.findByReservedBy(any())).thenReturn(List.of( ));

        RandomService randomService = new RandomService();
        TeamDeskFinderService teamDeskFinderService = new TeamDeskFinderService(reservationRepository, randomService);
        DeskService deskService = new DeskService(deskRepository, teamDeskFinderService, randomService);


        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(deskRepository, reservationRepository, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setSitCloseToTeam(true);
        request.setTeamMembers(List.of("member1"));

        var response = deskController.reserveDesk(request);

        assertEquals("<409 CONFLICT Conflict,Desk is not available,[]>", response.toString());
    }
    @Test
    void test17() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        var desk = new Desk();
        desk.setId(1L);
        when(deskRepository.findByAvailable(true)).thenReturn(List.of(desk));

        ReservationRepository reservationRepository = mock(ReservationRepository.class);

        when(reservationRepository.findByReservedBy(any())).thenReturn(List.of( ));

        RandomService randomService = new RandomService();
        TeamDeskFinderService teamDeskFinderService = new TeamDeskFinderService(reservationRepository, randomService);
        DeskService deskService = new DeskService(deskRepository, teamDeskFinderService, randomService);


        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);
        var deskController = new DeskController(deskRepository, reservationRepository, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setSitCloseToTeam(false);
        request.setTeamMembers(List.of("member1"));

        var response = deskController.reserveDesk(request);

        assertEquals("<409 CONFLICT Conflict,Desk is not available,[]>", response.toString());
    }
}
