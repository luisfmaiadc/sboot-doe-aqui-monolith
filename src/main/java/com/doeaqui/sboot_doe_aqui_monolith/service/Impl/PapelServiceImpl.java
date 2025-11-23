package com.doeaqui.sboot_doe_aqui_monolith.service.Impl;

import com.doeaqui.sboot_doe_aqui_monolith.domain.Papel;
import com.doeaqui.sboot_doe_aqui_monolith.repository.PapelRepository;
import com.doeaqui.sboot_doe_aqui_monolith.service.PapelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PapelServiceImpl implements PapelService {

    private final PapelRepository repository;

    @Override
    @Cacheable("papeisUsuarios")
    public List<Papel> getPapeisUsuarios() {
        log.info("[PapelServiceImpl] Buscando papéis de usuários.");
        return repository.getPapeisUsuarios();
    }

    @Override
    public Papel getPapelById(Integer idPapel) {
        log.debug("[PapelServiceImpl] Buscando papel com ID: {}", idPapel);
        List<Papel> papelList = getPapeisUsuarios();
        return papelList.stream()
                .filter(p -> p.getId().equals(idPapel.byteValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Papel não encontrado."));
    }
}