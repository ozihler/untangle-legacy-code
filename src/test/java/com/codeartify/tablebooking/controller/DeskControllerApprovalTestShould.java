package com.codeartify.tablebooking.controller;

import com.codeartify.tablebooking.dto.ReservationRequest;
import com.codeartify.tablebooking.repository.DeskRepository;
import com.codeartify.tablebooking.service.DeskService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
