package com.codeartify.tablebooking.desk_reservation.adapter.data_access;

import com.codeartify.tablebooking.desk_reservation.domain.AvailableDesks;
import com.codeartify.tablebooking.desk_reservation.domain.Team;
import com.codeartify.tablebooking.desk_reservation.domain.TeamMemberReservation;
import com.codeartify.tablebooking.desk_reservation.domain.TeamMemberUsername;
import com.codeartify.tablebooking.model.Desk;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReservationRepository {
    public final com.codeartify.tablebooking.repository.ReservationRepository reservationRepository;

    public ReservationRepository(com.codeartify.tablebooking.repository.ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public TeamMemberReservation findReservationsOf(TeamMemberUsername teamMemberUsername) {
        var reservationsOfTeamMember = this.reservationRepository.findByReservedBy(teamMemberUsername.value());

        return new TeamMemberReservation(reservationsOfTeamMember);
    }

    public List<Desk> findDesksOfTeam(Team team, AvailableDesks availableDesks) {
        var teamDeskIds = findReservationsOf(team.findFirst()).deskIds();

        return availableDesks.findTeamDesks(teamDeskIds);
    }
}
