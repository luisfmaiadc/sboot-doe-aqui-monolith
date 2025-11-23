package com.doeaqui.sboot_doe_aqui_monolith.controller;

import com.doeaqui.sboot_doe_aqui_monolith.api.PapelApiDelegate;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Papel;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.PapelMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.PapelResponse;
import com.doeaqui.sboot_doe_aqui_monolith.service.PapelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PapelApiImpl implements PapelApiDelegate {

    private final PapelService service;
    private final PapelMapper mapper;

    @Override
    public ResponseEntity<List<PapelResponse>> getPapeisUsuarios() {
        List<Papel> papelList = service.getPapeisUsuarios();
        List<PapelResponse> responseList = mapper.toPapelResponseList(papelList);
        return ResponseEntity.ok(responseList);
    }
}