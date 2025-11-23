package com.doeaqui.sboot_doe_aqui_monolith.mapper;

import com.doeaqui.sboot_doe_aqui_monolith.domain.Hemocentro;
import com.doeaqui.sboot_doe_aqui_monolith.model.HemocentroPorLocalizacaoResponse;
import com.doeaqui.sboot_doe_aqui_monolith.model.HemocentroResponse;
import com.doeaqui.sboot_doe_aqui_monolith.model.NewHemocentroRequest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = EnderecoMapper.class)
public interface HemocentroMapper {

    Hemocentro toHemocentro(NewHemocentroRequest request);
    HemocentroResponse toHemocentroResponse(Hemocentro hemocentro);
    List<HemocentroResponse> toHemocentroResponseList(List<Hemocentro> hemocentroList);
    HemocentroPorLocalizacaoResponse toHemocentroPorLocalizacaoResponse(Hemocentro hemocentro);
}