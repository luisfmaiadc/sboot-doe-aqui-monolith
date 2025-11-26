package com.doeaqui.sboot_doe_aqui_monolith.controller;

import com.doeaqui.sboot_doe_aqui_monolith.domain.Doacao;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.DoacaoMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.DoacaoResponse;
import com.doeaqui.sboot_doe_aqui_monolith.model.NewDoacaoRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UpdateDoacaoRequest;
import com.doeaqui.sboot_doe_aqui_monolith.service.DoacaoService;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoacaoApiImplTest {

    @InjectMocks
    private DoacaoApiImpl doacaoApi;

    @Mock
    private DoacaoService service;

    @Mock
    private DoacaoMapper mapper;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void postNewDoacao_shouldReturnCreated() {
        NewDoacaoRequest request = new NewDoacaoRequest();
        Doacao domainObject = new Doacao();
        domainObject.setId(1);
        DoacaoResponse responseDto = new DoacaoResponse().id(1);

        when(service.postNewDoacao(any(NewDoacaoRequest.class))).thenReturn(domainObject);
        when(mapper.toDoacaoResponse(any(Doacao.class))).thenReturn(responseDto);

        ResponseEntity<DoacaoResponse> response = doacaoApi.postNewDoacao(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
        assertEquals(URI.create("http://localhost/1"), response.getHeaders().getLocation());
    }

    @Test
    void getDoacaoInfoById_shouldReturnOk() {
        Integer doacaoId = 1;
        Doacao domainObject = new Doacao();
        domainObject.setId(doacaoId);
        DoacaoResponse responseDto = new DoacaoResponse().id(doacaoId);

        when(service.getDoacaoInfoById(doacaoId)).thenReturn(domainObject);
        when(mapper.toDoacaoResponse(domainObject)).thenReturn(responseDto);

        ResponseEntity<DoacaoResponse> response = doacaoApi.getDoacaoInfoById(doacaoId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(doacaoId, response.getBody().getId());
    }

    @Test
    void getDoacaoByFilter_shouldReturnOk() {
        Doacao domainObject = new Doacao();
        DoacaoResponse responseDto = new DoacaoResponse();
        List<Doacao> domainList = Collections.singletonList(domainObject);
        List<DoacaoResponse> responseList = Collections.singletonList(responseDto);

        when(service.getDoacaoByFilter(any(), any(), any(), any())).thenReturn(domainList);
        when(mapper.toDoacaoResponseList(domainList)).thenReturn(responseList);

        ResponseEntity<List<DoacaoResponse>> response = doacaoApi.getDoacaoByFilter(1, 1, LocalDate.now(), 500);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void patchDoacaoInfo_shouldReturnOk() {
        Integer doacaoId = 1;
        UpdateDoacaoRequest request = new UpdateDoacaoRequest();
        Doacao domainObject = new Doacao();
        domainObject.setId(doacaoId);
        DoacaoResponse responseDto = new DoacaoResponse().id(doacaoId);

        when(service.patchDoacaoInfo(anyInt(), any(UpdateDoacaoRequest.class))).thenReturn(domainObject);
        when(mapper.toDoacaoResponse(domainObject)).thenReturn(responseDto);

        ResponseEntity<DoacaoResponse> response = doacaoApi.patchDoacaoInfo(doacaoId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(doacaoId, response.getBody().getId());
    }
}