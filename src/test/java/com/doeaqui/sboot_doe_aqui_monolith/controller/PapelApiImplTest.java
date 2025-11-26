package com.doeaqui.sboot_doe_aqui_monolith.controller;

import com.doeaqui.sboot_doe_aqui_monolith.domain.Papel;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.PapelMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.PapelResponse;
import com.doeaqui.sboot_doe_aqui_monolith.service.PapelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PapelApiImplTest {

    @InjectMocks
    private PapelApiImpl papelApi;

    @Mock
    private PapelService service;

    @Mock
    private PapelMapper mapper;

    @Test
    void getPapeisUsuarios_shouldReturnOk() {
        Papel papel = new Papel();
        papel.setId((byte) 1);
        papel.setNome("ADMIN");

        PapelResponse papelResponse = new PapelResponse().id("1").nome("ADMIN");

        when(service.getPapeisUsuarios()).thenReturn(Collections.singletonList(papel));
        when(mapper.toPapelResponseList(Collections.singletonList(papel))).thenReturn(Collections.singletonList(papelResponse));

        ResponseEntity<List<PapelResponse>> response = papelApi.getPapeisUsuarios();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }
}