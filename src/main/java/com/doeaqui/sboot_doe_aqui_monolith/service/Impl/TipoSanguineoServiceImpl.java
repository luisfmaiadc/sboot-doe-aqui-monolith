package com.doeaqui.sboot_doe_aqui_monolith.service.Impl;

import com.doeaqui.sboot_doe_aqui_monolith.domain.TipoSanguineo;
import com.doeaqui.sboot_doe_aqui_monolith.repository.TipoSanguineoRepository;
import com.doeaqui.sboot_doe_aqui_monolith.service.TipoSanguineoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TipoSanguineoServiceImpl implements TipoSanguineoService {

    private final TipoSanguineoRepository repository;

    private static final Map<String, List<String>> COMPATIBILIDADE_DOADOR = Map.of(
            "A+", List.of("A+", "AB+"),
            "A-", List.of("A+", "A-", "AB+", "AB-"),
            "B+", List.of("B+", "AB+"),
            "B-", List.of("B+", "B-", "AB+", "AB-"),
            "AB+", List.of("AB+"),
            "AB-", List.of("AB+", "AB-"),
            "O+", List.of("A+", "B+", "O+", "AB+"),
            "O-", List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    );

    private static Map<String, Byte> tipoSanguineoStringToIdMap;
    private static Map<Byte, List<Byte>> compatibilidadePorId;

    private void initializeCompatibilityMaps() {
        if (tipoSanguineoStringToIdMap == null) {
            synchronized (TipoSanguineoServiceImpl.class) {
                if (tipoSanguineoStringToIdMap == null) {
                    List<TipoSanguineo> tiposSanguineos = getTiposSanguineos();
                    tipoSanguineoStringToIdMap = new HashMap<>();
                    tiposSanguineos.forEach(ts -> tipoSanguineoStringToIdMap.put(ts.getTipo() + ts.getFator(), ts.getId()));

                    compatibilidadePorId = new HashMap<>();
                    COMPATIBILIDADE_DOADOR.forEach((doador, receptores) -> {
                        Byte doadorId = tipoSanguineoStringToIdMap.get(doador);
                        List<Byte> receptoresIds = receptores.stream().map(tipoSanguineoStringToIdMap::get).toList();
                        compatibilidadePorId.put(doadorId, receptoresIds);
                    });
                }
            }
        }
    }

    @Override
    @Cacheable("tiposSanguineos")
    public List<TipoSanguineo> getTiposSanguineos() {
        log.info("[TipoSanguineoServiceImpl] Buscando tipos sanguíneos.");
        return repository.getTiposSanguineos();
    }

    @Override
    public String getTipoSanguineoById(Byte idTipoSanguineo) {
        log.debug("[TipoSanguineoServiceImpl] Buscando tipo sanguíneo com ID: {}", idTipoSanguineo);
        List<TipoSanguineo> tipoSanguineoList = getTiposSanguineos();
        return tipoSanguineoList.stream()
                .filter(t -> t.getId().equals(idTipoSanguineo))
                .findFirst()
                .map(t -> t.getTipo() + t.getFator())
                .orElseThrow(() -> new IllegalArgumentException("Tipo sanguíneo inválido."));
    }

    @Override
    public void validateBloodCompatible(Byte idReceptor, Byte idDoador) {
        log.debug("[TipoSanguineoServiceImpl] Iniciando validação de compatibilidade sanguinea de receptor {} com doador {}.", idReceptor, idDoador);
        List<Byte> receptoresCompativeis = getTipoSanguineoCompativel(idDoador);
        if (!receptoresCompativeis.contains(idReceptor)) {
            throw new IllegalArgumentException("Tipo sanguíneo incompatível.");
        }
    }

    @Override
    public List<Byte> getTipoSanguineoCompativel(Byte idTipoSanguineo) {
        initializeCompatibilityMaps();
        return compatibilidadePorId.getOrDefault(idTipoSanguineo, Collections.emptyList());
    }
}