package com.doeaqui.sboot_doe_aqui_monolith.service;

import com.doeaqui.sboot_doe_aqui_monolith.domain.Hemocentro;
import com.doeaqui.sboot_doe_aqui_monolith.model.HemocentroPorLocalizacaoResponse;
import com.doeaqui.sboot_doe_aqui_monolith.model.NewHemocentroRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UpdateHemocentroRequest;

import java.util.List;

public interface HemocentroService {

    Hemocentro postNewHemocentro(NewHemocentroRequest request);
    Hemocentro getHemocentroInfoById(Integer idHemocentro);
    List<Hemocentro> getHemocentroByFilter(String nome, String telefone, String email);
    Hemocentro patchHemocentroInfo(Integer idHemocentro, UpdateHemocentroRequest updateHemocentroRequest);
    void deleteHemocentro(Integer idHemocentro);
    List<HemocentroPorLocalizacaoResponse> getHemocentroByLocation(Double latitude, Double longitude, Integer raio);
}