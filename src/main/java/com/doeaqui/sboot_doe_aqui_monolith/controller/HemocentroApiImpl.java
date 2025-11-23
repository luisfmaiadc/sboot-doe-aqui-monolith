package com.doeaqui.sboot_doe_aqui_monolith.controller;

import com.doeaqui.sboot_doe_aqui_monolith.api.HemocentroApiDelegate;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Hemocentro;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.HemocentroMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.HemocentroPorLocalizacaoResponse;
import com.doeaqui.sboot_doe_aqui_monolith.model.HemocentroResponse;
import com.doeaqui.sboot_doe_aqui_monolith.model.NewHemocentroRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UpdateHemocentroRequest;
import com.doeaqui.sboot_doe_aqui_monolith.service.HemocentroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class HemocentroApiImpl implements HemocentroApiDelegate {

    private final HemocentroService service;
    private final HemocentroMapper mapper;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HemocentroResponse> postNewHemocentro(NewHemocentroRequest newHemocentroRequest) {
        Hemocentro newHemocentro = service.postNewHemocentro(newHemocentroRequest);
        HemocentroResponse response = mapper.toHemocentroResponse(newHemocentro);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Override
    public ResponseEntity<HemocentroResponse> getHemocentroInfoById(Integer idHemocentro) {
        Hemocentro hemocentro = service.getHemocentroInfoById(idHemocentro);
        HemocentroResponse response = mapper.toHemocentroResponse(hemocentro);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<HemocentroResponse>> getHemocentroByFilter(String nome, String telefone, String email) {
        List<Hemocentro> hemocentroList = service.getHemocentroByFilter(nome, telefone, email);
        List<HemocentroResponse> responseList = mapper.toHemocentroResponseList(hemocentroList);
        return ResponseEntity.ok(responseList);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HemocentroResponse> patchHemocentroInfo(Integer idHemocentro, UpdateHemocentroRequest updateHemocentroRequest) {
        Hemocentro hemocentro = service.patchHemocentroInfo(idHemocentro, updateHemocentroRequest);
        HemocentroResponse response = mapper.toHemocentroResponse(hemocentro);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHemocentro(Integer idHemocentro) {
        service.deleteHemocentro(idHemocentro);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'DOADOR', 'DOADOR_PACIENTE')")
    public ResponseEntity<List<HemocentroPorLocalizacaoResponse>> getHemocentroByLocation(Double latitude, Double longitude, Integer raio) {
        List<HemocentroPorLocalizacaoResponse> hemocentroList = service.getHemocentroByLocation(latitude, longitude, raio);
        return ResponseEntity.ok(hemocentroList);
    }
}