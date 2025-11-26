package com.doeaqui.sboot_doe_aqui_monolith.controller;

import com.doeaqui.sboot_doe_aqui_monolith.domain.TipoSanguineo;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.TipoSanguineoMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.TipoSanguineoResponse;
import com.doeaqui.sboot_doe_aqui_monolith.service.TipoSanguineoService;
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
class TipoSanguineoApiImplTest {

    @InjectMocks
    private TipoSanguineoApiImpl tipoSanguineoApi;

    @Mock
    private TipoSanguineoService service;

    @Mock
    private TipoSanguineoMapper mapper;

    @Test
    void getTiposSanguineos_shouldReturnOk() {
        TipoSanguineo tipoSanguineo = new TipoSanguineo();
        tipoSanguineo.setId((byte) 1);
        tipoSanguineo.setTipo("A");
        tipoSanguineo.setFator('+');

        TipoSanguineoResponse tipoSanguineoResponse = new TipoSanguineoResponse().id("1").tipo("A").fator("+");

        when(service.getTiposSanguineos()).thenReturn(Collections.singletonList(tipoSanguineo));
        when(mapper.toTipoSanguineoResponseList(Collections.singletonList(tipoSanguineo)))
                .thenReturn(Collections.singletonList(tipoSanguineoResponse));

        ResponseEntity<List<TipoSanguineoResponse>> response = tipoSanguineoApi.getTiposSanguineos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }
}