package com.codeartify.tablebooking.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ReservationRequest {
    private Long deskId;
    private String reservedBy;
    private String reservationType;
    private List<String> teamMembers;
    private boolean isRecurring;
    private String recurrencePattern;
    private boolean sitCloseToTeam;
    private String purpose;
    private boolean needsMonitor;
    private boolean needsAdjustableDesk;
    private String startTime;
    private String endTime;
}
