package com.codeartify.tablebooking.controller;

import com.codeartify.tablebooking.dto.ReservationRequest;
import com.codeartify.tablebooking.model.Desk;
import com.codeartify.tablebooking.model.Reservation;
import com.codeartify.tablebooking.repository.DeskRepository;
import com.codeartify.tablebooking.repository.ReservationRepository;
import com.codeartify.tablebooking.service.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeskControllerApprovalTest {

    @Test
    void test() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        DeskService deskService = new DeskService(deskRepository, null, null);
        ReservationRequest request = new ReservationRequest();

        var deskController = new DeskController(deskRepository, null, null, deskService);

        var response = deskController.reserveDesk(request);

        assertEquals("<404 NOT_FOUND Not Found,[]>", response.toString());
    }

    @Test
    void test2() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        DeskService deskService = new DeskService(deskRepository, null, null);
        ReservationRequest request = new ReservationRequest();
        request.setDeskId(1L);

        var deskController = new DeskController(deskRepository, null, null, deskService);

        var response = deskController.reserveDesk(request);

        assertEquals("<404 NOT_FOUND Not Found,[]>", response.toString());
    }

    @Test
    void test3() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        when(deskRepository.findByAvailable(true)).thenReturn(List.of(new Desk()));

        RandomService randomService = new RandomService();
        DeskService deskService = new DeskService(deskRepository, null, randomService);
        ReservationRequest request = new ReservationRequest();

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, null);
        var deskController = new DeskController(deskRepository, null, reservationService, deskService);

        var response = deskController.reserveDesk(request);

        assertEquals("<409 CONFLICT Conflict,Desk is not available,[]>", response.toString());
    }

    @Test
    void test4() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        when(deskRepository.findByAvailable(true)).thenReturn(List.of(new Desk()));
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        RandomService randomService = new RandomService();
        TeamDeskFinderService teamDeskFinderService = new TeamDeskFinderService(reservationRepository, randomService);
        DeskService deskService = new DeskService(deskRepository, teamDeskFinderService, randomService);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, null);

        var deskController = new DeskController(deskRepository, null, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setSitCloseToTeam(true);
        request.setTeamMembers(new ArrayList<>());

        var response = deskController.reserveDesk(request);

        assertEquals("<409 CONFLICT Conflict,Desk is not available,[]>", response.toString());
    }

    @Test
    void test5() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        var reservedDesk = new Desk();
        reservedDesk.setId(2L);
        when(deskRepository.findByAvailable(true)).thenReturn(List.of(reservedDesk));

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        var teamMemberReservation = new Reservation();
        var reservedBy = "team-member-name";
        teamMemberReservation.setReservedBy(reservedBy);
        teamMemberReservation.setDeskId(reservedDesk.getId());
        when(reservationRepository.findByReservedBy(reservedBy)).thenReturn(List.of(teamMemberReservation));

        RandomService randomService = new RandomService();
        TeamDeskFinderService teamDeskFinderService = new TeamDeskFinderService(reservationRepository, randomService);
        DeskService deskService = new DeskService(deskRepository, teamDeskFinderService, randomService);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, null);

        var deskController = new DeskController(deskRepository, null, reservationService, deskService);

        ReservationRequest request = new ReservationRequest();
        request.setSitCloseToTeam(true);
        request.setTeamMembers(List.of(reservedBy));

        var response = deskController.reserveDesk(request);

        assertEquals("<409 CONFLICT Conflict,Desk is not available,[]>", response.toString());
    }


}
