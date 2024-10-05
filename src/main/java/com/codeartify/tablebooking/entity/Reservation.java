package com.codeartify.tablebooking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long deskId;
    private String reservedBy;
    private String reservationType; // Options: "whole_day", "half_day", "one_hour"
    private boolean isRecurring;
    private String recurrencePattern; // Options: "daily", "weekly", "monthly"
    private String purpose; // e.g., "meeting", "focused work", "collaboration"
    private String startTime;
    private String endTime;

    @ElementCollection
    private List<String> teamMembers;


}
