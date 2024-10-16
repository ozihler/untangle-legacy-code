package com.codeartify.tablebooking.desk_reservation;

import com.codeartify.tablebooking.desk_reservation.adapter.presentation.DeskReservationController;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeskReservationControllerApprovalTest {

    @Test
    void test() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        DeskService deskService = new DeskService(deskRepository, null, null);
        ReservationRequest request = new ReservationRequest();

        var deskController = new DeskReservationController(deskRepository, null, new com.codeartify.tablebooking.desk_reservation.adapter.data_access.ReservationRepository(null));

        var response = deskController.reserveDesk(request);

        assertEquals("<404 NOT_FOUND Not Found,[]>", response.toString());
    }

    @Test
    void test2() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        DeskService deskService = new DeskService(deskRepository, null, null);
        ReservationRequest request = new ReservationRequest();
        request.setDeskId(1L);

        var deskController = new DeskReservationController(deskRepository, null, new com.codeartify.tablebooking.desk_reservation.adapter.data_access.ReservationRepository(null));

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
        var deskController = new DeskReservationController(deskRepository, reservationRepository, new com.codeartify.tablebooking.desk_reservation.adapter.data_access.ReservationRepository(reservationRepository));

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

        var deskController = new DeskReservationController(deskRepository, reservationRepository, new com.codeartify.tablebooking.desk_reservation.adapter.data_access.ReservationRepository(reservationRepository));

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

        var deskController = new DeskReservationController(deskRepository, reservationRepository, new com.codeartify.tablebooking.desk_reservation.adapter.data_access.ReservationRepository(reservationRepository));

        ReservationRequest request = new ReservationRequest();
        request.setSitCloseToTeam(true);
        request.setTeamMembers(List.of(reservedBy));

        var response = deskController.reserveDesk(request);

        assertEquals("<409 CONFLICT Conflict,Desk is not available,[]>", response.toString());
    }

    @Test
    void test6() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        var reservedDesk = new Desk();
        reservedDesk.setId(2L);
        reservedDesk.setAvailable(true);
        when(deskRepository.findByAvailable(true)).thenReturn(List.of(reservedDesk));

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        var teamMemberReservation = new Reservation();
        var reservedBy = "team-member-name";
        teamMemberReservation.setReservedBy(reservedBy);
        teamMemberReservation.setDeskId(reservedDesk.getId());
        teamMemberReservation.setStartTime("08:00:00");
        teamMemberReservation.setEndTime("17:00:00");
        when(reservationRepository.findByReservedBy(reservedBy)).thenReturn(List.of(teamMemberReservation));

        RandomService randomService = new RandomService();
        TeamDeskFinderService teamDeskFinderService = new TeamDeskFinderService(reservationRepository, randomService);
        DeskService deskService = new DeskService(deskRepository, teamDeskFinderService, randomService);
        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);

        var deskController = new DeskReservationController(deskRepository, reservationRepository, new com.codeartify.tablebooking.desk_reservation.adapter.data_access.ReservationRepository(reservationRepository));

        ReservationRequest request = new ReservationRequest();
        request.setSitCloseToTeam(true);
        request.setTeamMembers(List.of(reservedBy));
        request.setStartTime("08:00:00");
        request.setEndTime("17:00:00");

        var response = deskController.reserveDesk(request);

        assertEquals("<409 CONFLICT Conflict,Team member team-member-name already has a reservation during the selected time.,[]>", response.toString());
    }


    @Test
    void test7() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        var reservedDesk = new Desk();
        reservedDesk.setId(2L);
        reservedDesk.setAvailable(true);
        when(deskRepository.findByAvailable(true)).thenReturn(List.of(reservedDesk));

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        var teamMemberReservation = new Reservation();
        var reservedBy = "team-member-name";
        teamMemberReservation.setReservedBy(reservedBy);
        teamMemberReservation.setDeskId(reservedDesk.getId());
        teamMemberReservation.setStartTime("08:00:00");
        teamMemberReservation.setEndTime("17:00:00");
        when(reservationRepository.findByReservedBy(reservedBy)).thenReturn(List.of(teamMemberReservation));

        RandomService randomService = new RandomService();
        TeamDeskFinderService teamDeskFinderService = new TeamDeskFinderService(reservationRepository, randomService);
        DeskService deskService = new DeskService(deskRepository, teamDeskFinderService, randomService);
        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);

        var deskController = new DeskReservationController(deskRepository, reservationRepository, new com.codeartify.tablebooking.desk_reservation.adapter.data_access.ReservationRepository(reservationRepository));

        ReservationRequest request = new ReservationRequest();
        request.setSitCloseToTeam(true);
        request.setTeamMembers(List.of(reservedBy));
        request.setStartTime("08:00:00");
        request.setEndTime("17:00:00");
        request.setReservedBy(reservedBy);

        var response = deskController.reserveDesk(request);

        assertEquals("<409 CONFLICT Conflict,There is already a reservation for the selected time.,[]>", response.toString());
    }


    @Test
    void test8() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        var reservedDesk = new Desk();
        reservedDesk.setId(2L);
        reservedDesk.setAvailable(true);
        when(deskRepository.findByAvailable(true)).thenReturn(List.of(reservedDesk));

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        var teamMemberReservation = new Reservation();
        var reservedBy = "team-member-name";
        teamMemberReservation.setReservedBy(reservedBy);
        teamMemberReservation.setDeskId(reservedDesk.getId());
        teamMemberReservation.setStartTime("08:00:00");
        teamMemberReservation.setEndTime("17:00:00");
        when(reservationRepository.findByReservedBy(reservedBy)).thenReturn(List.of(teamMemberReservation));

        RandomService randomService = new RandomService();
        TeamDeskFinderService teamDeskFinderService = new TeamDeskFinderService(reservationRepository, randomService);
        DeskService deskService = new DeskService(deskRepository, teamDeskFinderService, randomService);
        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);

        var deskController = new DeskReservationController(deskRepository, reservationRepository, new com.codeartify.tablebooking.desk_reservation.adapter.data_access.ReservationRepository(reservationRepository));

        ReservationRequest request = new ReservationRequest();
        request.setSitCloseToTeam(true);
        request.setTeamMembers(List.of());
        request.setReservedBy(reservedBy);

        var response = deskController.reserveDesk(request);

        assertEquals("<201 CREATED Created,Desk(id=2, available=false, location=null, hasMonitor=false, isAdjustable=false),[]>", response.toString());
    }

    @Test
    void test9() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        var reservedDesk = new Desk();
        reservedDesk.setId(2L);
        reservedDesk.setAvailable(true);
        when(deskRepository.findByAvailable(true)).thenReturn(List.of(reservedDesk));

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        var teamMemberReservation = new Reservation();
        var reservedBy = "team-member-name";
        teamMemberReservation.setReservedBy(reservedBy);
        teamMemberReservation.setDeskId(reservedDesk.getId());
        teamMemberReservation.setStartTime("08:00:00");
        teamMemberReservation.setEndTime("17:00:00");
        when(reservationRepository.findByReservedBy(reservedBy)).thenReturn(List.of(teamMemberReservation));
        when(reservationRepository.save(any())).thenThrow(new RuntimeException());
        RandomService randomService = new RandomService();
        TeamDeskFinderService teamDeskFinderService = new TeamDeskFinderService(reservationRepository, randomService);
        DeskService deskService = new DeskService(deskRepository, teamDeskFinderService, randomService);
        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);

        var deskController = new DeskReservationController(deskRepository, reservationRepository, new com.codeartify.tablebooking.desk_reservation.adapter.data_access.ReservationRepository(reservationRepository));

        ReservationRequest request = new ReservationRequest();
        request.setSitCloseToTeam(true);
        request.setTeamMembers(List.of());
        request.setReservedBy(reservedBy);

        var response = deskController.reserveDesk(request);

        assertEquals("<500 INTERNAL_SERVER_ERROR Internal Server Error,An error occurred while saving the reservation: null,[]>", response.toString());
    }


    @Test
    void test10() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        var reservedDesk = new Desk();
        reservedDesk.setId(2L);
        reservedDesk.setAvailable(true);
        when(deskRepository.findByAvailable(true)).thenReturn(List.of(reservedDesk));

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        var teamMemberReservation = new Reservation();
        var reservedBy = "team-member-name";
        teamMemberReservation.setReservedBy(reservedBy);
        teamMemberReservation.setDeskId(reservedDesk.getId());
        teamMemberReservation.setStartTime("08:00:00");
        teamMemberReservation.setEndTime("17:00:00");
        when(reservationRepository.findByReservedBy(reservedBy)).thenReturn(List.of(teamMemberReservation));

        RandomService randomService = new RandomService();
        TeamDeskFinderService teamDeskFinderService = new TeamDeskFinderService(reservationRepository, randomService);
        DeskService deskService = new DeskService(deskRepository, teamDeskFinderService, randomService);
        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);

        var deskController = new DeskReservationController(deskRepository, reservationRepository, new com.codeartify.tablebooking.desk_reservation.adapter.data_access.ReservationRepository(reservationRepository));

        ReservationRequest request = new ReservationRequest();
        request.setSitCloseToTeam(true);
        request.setTeamMembers(List.of());
        request.setReservedBy(reservedBy);
        request.setRecurring(true);

        var response = deskController.reserveDesk(request);

        assertEquals("<400 BAD_REQUEST Bad Request,Recurring reservations must have a recurrence pattern.,[]>", response.toString());
    }


    @Test
    void test11() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        var reservedDesk = new Desk();
        reservedDesk.setId(2L);
        reservedDesk.setAvailable(true);
        when(deskRepository.findByAvailable(true)).thenReturn(List.of(reservedDesk));

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        var teamMemberReservation = new Reservation();
        var reservedBy = "team-member-name";
        teamMemberReservation.setReservedBy(reservedBy);
        teamMemberReservation.setDeskId(reservedDesk.getId());
        teamMemberReservation.setStartTime("08:00:00");
        teamMemberReservation.setEndTime("17:00:00");
        when(reservationRepository.findByReservedBy(reservedBy)).thenReturn(List.of(teamMemberReservation));

        RandomService randomService = new RandomService();
        TeamDeskFinderService teamDeskFinderService = new TeamDeskFinderService(reservationRepository, randomService);
        DeskService deskService = new DeskService(deskRepository, teamDeskFinderService, randomService);
        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);

        var deskController = new DeskReservationController(deskRepository, reservationRepository, new com.codeartify.tablebooking.desk_reservation.adapter.data_access.ReservationRepository(reservationRepository));

        ReservationRequest request = new ReservationRequest();
        request.setSitCloseToTeam(true);
        request.setTeamMembers(List.of());
        request.setReservedBy(reservedBy);
        request.setNeedsAdjustableDesk(true);

        var response = deskController.reserveDesk(request);

        assertEquals("<409 CONFLICT Conflict,Requested desk is not adjustable.,[]>", response.toString());
    }


    @Test
    void test12() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        var reservedDesk = new Desk();
        reservedDesk.setId(2L);
        reservedDesk.setAvailable(true);
        when(deskRepository.findByAvailable(true)).thenReturn(List.of(reservedDesk));

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        var teamMemberReservation = new Reservation();
        var reservedBy = "team-member-name";
        teamMemberReservation.setReservedBy(reservedBy);
        teamMemberReservation.setDeskId(reservedDesk.getId());
        teamMemberReservation.setStartTime("08:00:00");
        teamMemberReservation.setEndTime("17:00:00");
        when(reservationRepository.findByReservedBy(reservedBy)).thenReturn(List.of(teamMemberReservation));

        RandomService randomService = new RandomService();
        TeamDeskFinderService teamDeskFinderService = new TeamDeskFinderService(reservationRepository, randomService);
        DeskService deskService = new DeskService(deskRepository, teamDeskFinderService, randomService);
        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);

        var deskController = new DeskReservationController(deskRepository, reservationRepository, new com.codeartify.tablebooking.desk_reservation.adapter.data_access.ReservationRepository(reservationRepository));

        ReservationRequest request = new ReservationRequest();
        request.setSitCloseToTeam(true);
        request.setTeamMembers(List.of());
        request.setReservedBy(reservedBy);
        request.setNeedsMonitor(true);

        var response = deskController.reserveDesk(request);

        assertEquals("<409 CONFLICT Conflict,Requested desk does not have a monitor.,[]>", response.toString());
    }

    @Test
    void test13() {
        DeskRepository deskRepository = mock(DeskRepository.class);
        var reservedDesk = new Desk();
        reservedDesk.setId(2L);
        reservedDesk.setAvailable(true);
        reservedDesk.setHasMonitor(true);
        reservedDesk.setAdjustable(true);
        when(deskRepository.findByAvailable(true)).thenReturn(List.of(reservedDesk));

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        var teamMemberReservation = new Reservation();
        var reservedBy = "team-member-name";
        teamMemberReservation.setReservedBy(reservedBy);
        teamMemberReservation.setDeskId(reservedDesk.getId());
        teamMemberReservation.setStartTime("08:00:00");
        teamMemberReservation.setEndTime("17:00:00");
        when(reservationRepository.findByReservedBy(reservedBy)).thenReturn(List.of(teamMemberReservation));

        RandomService randomService = new RandomService();
        TeamDeskFinderService teamDeskFinderService = new TeamDeskFinderService(reservationRepository, randomService);
        DeskService deskService = new DeskService(deskRepository, teamDeskFinderService, randomService);
        DeskReservationCheckerService deskReservationCheckerService = new DeskReservationCheckerService(reservationRepository);
        ReservationService reservationService = new ReservationService(deskRepository, reservationRepository, deskReservationCheckerService);

        var deskController = new DeskReservationController(deskRepository, reservationRepository, new com.codeartify.tablebooking.desk_reservation.adapter.data_access.ReservationRepository(reservationRepository));

        ReservationRequest request = new ReservationRequest();
        request.setSitCloseToTeam(true);
        request.setTeamMembers(List.of());
        request.setReservedBy(reservedBy);
        request.setRecurring(true);
        request.setRecurrencePattern("every week");
        request.setNeedsAdjustableDesk(true);
        request.setNeedsMonitor(true);

        var response = deskController.reserveDesk(request);

        assertEquals("<201 CREATED Created,Desk(id=2, available=false, location=null, hasMonitor=true, isAdjustable=true),[]>", response.toString());
    }


}
