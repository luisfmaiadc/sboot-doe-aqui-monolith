package com.doeaqui.sboot_doe_aqui_monolith.repository;

import com.doeaqui.sboot_doe_aqui_monolith.domain.Hemocentro;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface HemocentroRepository {

    int postNewHemocentro(Hemocentro newHemocentro);
    Optional<Hemocentro> getHemocentroInfoById(Integer idHemocentro);
    List<Hemocentro> getHemocentroByFilter(String nome, String telefone, String email);
    void patchHemocentroInfo(Hemocentro hemocentro);
    void deleteHemocentro(Integer idHemocentro);
    Set<Integer> getHemocentroIfHasSolicitacaoDoacao(List<Integer> hemocentroIdList, Integer idUsuario, List<Byte> tipoSanguineoIdList);
    List<Hemocentro> getHemocentrosInfoByIds(List<Integer> nearbyHemocentroIds);
}