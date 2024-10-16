package com.codeartify.tablebooking.desk_reservation.domain;

import com.codeartify.tablebooking.model.Reservation;

import java.util.List;

public record TeamMemberReservation(List<Reservation> reservationsOfTeamMember) {

    public List<Long> deskIds() {
        return reservationsOfTeamMember
                .stream()
                .map(Reservation::getDeskId)
                .toList();
    }
}
