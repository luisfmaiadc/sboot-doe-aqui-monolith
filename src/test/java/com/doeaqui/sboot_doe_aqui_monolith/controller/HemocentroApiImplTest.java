package com.doeaqui.sboot_doe_aqui_monolith.controller;

import com.doeaqui.sboot_doe_aqui_monolith.domain.Hemocentro;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.HemocentroMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.HemocentroPorLocalizacaoResponse;
import com.doeaqui.sboot_doe_aqui_monolith.model.HemocentroResponse;
import com.doeaqui.sboot_doe_aqui_monolith.model.NewHemocentroRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UpdateHemocentroRequest;
import com.doeaqui.sboot_doe_aqui_monolith.service.HemocentroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HemocentroApiImplTest {

    @InjectMocks
    private HemocentroApiImpl hemocentroApi;

    @Mock
    private HemocentroService service;

    @Mock
    private HemocentroMapper mapper;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void postNewHemocentro_shouldReturnCreated() {
        NewHemocentroRequest request = new NewHemocentroRequest();
        Hemocentro domainObject = new Hemocentro();
        domainObject.setId(1);
        HemocentroResponse responseDto = new HemocentroResponse().id(1);

        when(service.postNewHemocentro(any(NewHemocentroRequest.class))).thenReturn(domainObject);
        when(mapper.toHemocentroResponse(any(Hemocentro.class))).thenReturn(responseDto);

        ResponseEntity<HemocentroResponse> response = hemocentroApi.postNewHemocentro(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
        assertEquals(URI.create("http://localhost/1"), response.getHeaders().getLocation());
    }

    @Test
    void getHemocentroInfoById_shouldReturnOk() {
        Integer hemocentroId = 1;
        Hemocentro domainObject = new Hemocentro();
        domainObject.setId(hemocentroId);
        HemocentroResponse responseDto = new HemocentroResponse().id(hemocentroId);

        when(service.getHemocentroInfoById(hemocentroId)).thenReturn(domainObject);
        when(mapper.toHemocentroResponse(domainObject)).thenReturn(responseDto);

        ResponseEntity<HemocentroResponse> response = hemocentroApi.getHemocentroInfoById(hemocentroId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(hemocentroId, response.getBody().getId());
    }

    @Test
    void getHemocentroByFilter_shouldReturnOk() {
        Hemocentro domainObject = new Hemocentro();
        HemocentroResponse responseDto = new HemocentroResponse();
        List<Hemocentro> domainList = Collections.singletonList(domainObject);
        List<HemocentroResponse> responseList = Collections.singletonList(responseDto);

        when(service.getHemocentroByFilter(null, null, null)).thenReturn(domainList);
        when(mapper.toHemocentroResponseList(domainList)).thenReturn(responseList);

        ResponseEntity<List<HemocentroResponse>> response = hemocentroApi.getHemocentroByFilter(null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void patchHemocentroInfo_shouldReturnOk() {
        Integer hemocentroId = 1;
        UpdateHemocentroRequest request = new UpdateHemocentroRequest();
        Hemocentro domainObject = new Hemocentro();
        domainObject.setId(hemocentroId);
        HemocentroResponse responseDto = new HemocentroResponse().id(hemocentroId);

        when(service.patchHemocentroInfo(anyInt(), any(UpdateHemocentroRequest.class))).thenReturn(domainObject);
        when(mapper.toHemocentroResponse(domainObject)).thenReturn(responseDto);

        ResponseEntity<HemocentroResponse> response = hemocentroApi.patchHemocentroInfo(hemocentroId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(hemocentroId, response.getBody().getId());
    }

    @Test
    void deleteHemocentro_shouldReturnNoContent() {
        Integer hemocentroId = 1;
        doNothing().when(service).deleteHemocentro(hemocentroId);

        ResponseEntity<Void> response = hemocentroApi.deleteHemocentro(hemocentroId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void getHemocentroByLocation_shouldReturnOk() {
        List<HemocentroPorLocalizacaoResponse> responseList = Collections.singletonList(new HemocentroPorLocalizacaoResponse());
        when(service.getHemocentroByLocation(any(), any(), any())).thenReturn(responseList);

        ResponseEntity<List<HemocentroPorLocalizacaoResponse>> response = hemocentroApi.getHemocentroByLocation(-23.5, -46.6, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }
}