package com.codeartify.tablebooking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Entity
public class Desk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type; // Options: "standing", "shared", "regular", "private_office"
    private boolean available;
    private String location; // e.g., "Floor 1, Zone A"
    private boolean nearWindow;
    private boolean hasMonitor;
    private boolean isAdjustable;
    private boolean reservedForManager;

}
