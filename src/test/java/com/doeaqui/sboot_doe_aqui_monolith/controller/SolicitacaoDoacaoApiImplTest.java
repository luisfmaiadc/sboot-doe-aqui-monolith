package com.doeaqui.sboot_doe_aqui_monolith.controller;

import com.doeaqui.sboot_doe_aqui_monolith.domain.SolicitacaoDoacao;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.SolicitacaoDoacaoMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.NewSolicitacaoDoacaoRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.SolicitacaoDoacaoResponse;
import com.doeaqui.sboot_doe_aqui_monolith.model.UpdateSolicitacaoDoacaoRequest;
import com.doeaqui.sboot_doe_aqui_monolith.service.SolicitacaoDoacaoService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitacaoDoacaoApiImplTest {

    @InjectMocks
    private SolicitacaoDoacaoApiImpl solicitacaoDoacaoApi;

    @Mock
    private SolicitacaoDoacaoService service;

    @Mock
    private SolicitacaoDoacaoMapper mapper;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void postNewSolicitacaoDoacao_shouldReturnCreated() {
        NewSolicitacaoDoacaoRequest request = new NewSolicitacaoDoacaoRequest();
        SolicitacaoDoacao domainObject = new SolicitacaoDoacao();
        domainObject.setId(1);
        SolicitacaoDoacaoResponse responseDto = new SolicitacaoDoacaoResponse().id(1);

        when(service.postNewSolicitacaoDoacao(any(NewSolicitacaoDoacaoRequest.class))).thenReturn(domainObject);
        when(mapper.toSolicitacaoDoacaoResponse(any(SolicitacaoDoacao.class))).thenReturn(responseDto);

        ResponseEntity<SolicitacaoDoacaoResponse> response = solicitacaoDoacaoApi.postNewSolicitacaoDoacao(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
        assertEquals(URI.create("http://localhost/1"), response.getHeaders().getLocation());
    }

    @Test
    void getSolicitacaoDoacaoByFilter_shouldReturnOk() {
        SolicitacaoDoacao domainObject = new SolicitacaoDoacao();
        SolicitacaoDoacaoResponse responseDto = new SolicitacaoDoacaoResponse();
        List<SolicitacaoDoacao> domainList = Collections.singletonList(domainObject);
        List<SolicitacaoDoacaoResponse> responseList = Collections.singletonList(responseDto);

        when(service.getSolicitacaoDoacaoByFilter(null, null, null, null, null, null)).thenReturn(domainList);
        when(mapper.toSolicitacaoDoacaoResponseList(domainList)).thenReturn(responseList);

        ResponseEntity<List<SolicitacaoDoacaoResponse>> response = solicitacaoDoacaoApi.getSolicitacaoDoacaoByFilter(null, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getSolicitacaoDoacaoInfoById_shouldReturnOk() {
        Integer solicitacaoId = 1;
        SolicitacaoDoacao domainObject = new SolicitacaoDoacao();
        domainObject.setId(solicitacaoId);
        SolicitacaoDoacaoResponse responseDto = new SolicitacaoDoacaoResponse().id(solicitacaoId);

        when(service.getSolicitacaoDoacaoInfoById(solicitacaoId)).thenReturn(domainObject);
        when(mapper.toSolicitacaoDoacaoResponse(domainObject)).thenReturn(responseDto);

        ResponseEntity<SolicitacaoDoacaoResponse> response = solicitacaoDoacaoApi.getSolicitacaoDoacaoInfoById(solicitacaoId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(solicitacaoId, response.getBody().getId());
    }

    @Test
    void patchSolicitacaoDoacaoInfo_shouldReturnOk() {
        Integer solicitacaoId = 1;
        UpdateSolicitacaoDoacaoRequest request = new UpdateSolicitacaoDoacaoRequest();
        SolicitacaoDoacao domainObject = new SolicitacaoDoacao();
        domainObject.setId(solicitacaoId);
        SolicitacaoDoacaoResponse responseDto = new SolicitacaoDoacaoResponse().id(solicitacaoId);

        when(service.patchSolicitacaoDoacaoInfo(anyInt(), any(UpdateSolicitacaoDoacaoRequest.class))).thenReturn(domainObject);
        when(mapper.toSolicitacaoDoacaoResponse(domainObject)).thenReturn(responseDto);

        ResponseEntity<SolicitacaoDoacaoResponse> response = solicitacaoDoacaoApi.patchSolicitacaoDoacaoInfo(solicitacaoId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(solicitacaoId, response.getBody().getId());
    }
}