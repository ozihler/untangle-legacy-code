package com.codeartify.tablebooking.desk_reservation.domain;

import java.util.List;

public record Team(List<String> teamMembers) {
    public TeamMemberUsername findFirst() {
        return new TeamMemberUsername(teamMembers.stream().findFirst().orElse(null));
    }
}
