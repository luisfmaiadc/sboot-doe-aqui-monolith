package com.doeaqui.sboot_doe_aqui_monolith.service;

import com.doeaqui.sboot_doe_aqui_monolith.domain.Papel;

import java.util.List;

public interface PapelService {

    List<Papel> getPapeisUsuarios();
    Papel getPapelById(Integer idPapel);
}