package com.doeaqui.sboot_doe_aqui_monolith.service.Impl;

import com.doeaqui.sboot_doe_aqui_monolith.config.exception.ResourceNotFoundException;
import com.doeaqui.sboot_doe_aqui_monolith.config.security.CustomUserDetails;
import com.doeaqui.sboot_doe_aqui_monolith.domain.EnderecoHemocentro;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Hemocentro;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.HemocentroMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.HemocentroPorLocalizacaoResponse;
import com.doeaqui.sboot_doe_aqui_monolith.model.NewHemocentroRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UpdateHemocentroRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UsuarioResponse;
import com.doeaqui.sboot_doe_aqui_monolith.repository.EnderecoHemocentroRepository;
import com.doeaqui.sboot_doe_aqui_monolith.repository.HemocentroRepository;
import com.doeaqui.sboot_doe_aqui_monolith.service.HemocentroService;
import com.doeaqui.sboot_doe_aqui_monolith.service.TipoSanguineoService;
import com.doeaqui.sboot_doe_aqui_monolith.service.UsuarioService;
import com.doeaqui.sboot_doe_aqui_monolith.util.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HemocentroServiceImpl implements HemocentroService {

    private final HemocentroRepository repository;
    private final EnderecoHemocentroRepository enderecoHemocentroRepository;
    private final HemocentroMapper mapper;
    private final UsuarioService usuarioService;
    private final TipoSanguineoService tipoSanguineoService;

    @Override
    @Transactional
    public Hemocentro postNewHemocentro(NewHemocentroRequest request) {
        log.info("[HemocentroServiceImpl] Iniciando cadastro de novo hemocentro.");
        Hemocentro newHemocentro = mapper.toHemocentro(request);
        newHemocentro.setAtivo(Boolean.TRUE);
        int idNewHemocentro = repository.postNewHemocentro(newHemocentro);
        newHemocentro.getEndereco().setId(idNewHemocentro);
        enderecoHemocentroRepository.save(newHemocentro.getEndereco());
        log.info("[HemocentroServiceImpl] Hemocentro cadastrado com sucesso. ID gerado: {}.", idNewHemocentro);
        return getHemocentroInfoById(idNewHemocentro);
    }

    @Override
    public Hemocentro getHemocentroInfoById(Integer idHemocentro) {
        log.info("[HemocentroServiceImpl] Buscando informações do hemocentro com ID: {}", idHemocentro);
        Optional<Hemocentro> optionalHemocentro = repository.getHemocentroInfoById(idHemocentro);
        if (optionalHemocentro.isEmpty()) throw new ResourceNotFoundException("Hemocentro não encontrado.");
        Hemocentro hemocentro = optionalHemocentro.get();
        EnderecoHemocentro endereco = getEnderecoHemocentro(hemocentro);
        hemocentro.setEndereco(endereco);
        log.info("[HemocentroServiceImpl] Informações do hemocentro {} encontradas com sucesso.", idHemocentro);
        return hemocentro;
    }

    @Override
    public List<Hemocentro> getHemocentroByFilter(String nome, String telefone, String email) {
        log.info("[HemocentroServiceImpl] Buscando hemocentros por filtros.");
        AppUtils.requireAtLeastOneNonNull(Arrays.asList(nome, telefone, email));
        if (nome != null && !nome.trim().isEmpty()) nome = "%" + nome + "%";
        List<Hemocentro> hemocentroList = repository.getHemocentroByFilter(nome, telefone, email);
        if (hemocentroList.isEmpty()) {
            log.warn("[HemocentroServiceImpl] Nenhum hemocentro encontrado por filtros.");
            return hemocentroList;
        }
        hemocentroList.forEach(h -> {
            EnderecoHemocentro endereco = getEnderecoHemocentro(h);
            h.setEndereco(endereco);
        });
        log.info("[HemocentroServiceImpl] {} Hemocentros encontrados por filtros.", hemocentroList.size());
        return hemocentroList;
    }

    @Override
    @Transactional
    public Hemocentro patchHemocentroInfo(Integer idHemocentro, UpdateHemocentroRequest updateHemocentroRequest) {
        log.info("[HemocentroServiceImpl] Iniciando atualização de informações do hemocentro com ID: {}", idHemocentro);
        AppUtils.requireAtLeastOneNonNull(Arrays.asList(updateHemocentroRequest.getTelefone(),
                updateHemocentroRequest.getEmail(), updateHemocentroRequest.getAtivo()));

        Hemocentro hemocentro = getHemocentroInfoById(idHemocentro);

        boolean hemocentroFieldsChanged = applyHemocentroUpdates(hemocentro, updateHemocentroRequest);
        if(!hemocentroFieldsChanged) throw new IllegalArgumentException("Informe ao menos um campo para atualizar.");

        repository.patchHemocentroInfo(hemocentro);
        log.info("[HemocentroServiceImpl] Hemocentro com ID {} atualizado com sucesso.", idHemocentro);
        return getHemocentroInfoById(idHemocentro);
    }

    @Override
    @Transactional
    public void deleteHemocentro(Integer idHemocentro) {
        log.info("[HemocentroServiceImpl] Iniciando inativação do hemocentro com ID: {}", idHemocentro);
        Hemocentro hemocentro = getHemocentroInfoById(idHemocentro);
        if (Objects.equals(hemocentro.getAtivo(), Boolean.FALSE)) throw new IllegalArgumentException("Hemocentro já inativado.");
        repository.deleteHemocentro(idHemocentro);
        log.info("[HemocentroServiceImpl] Hemocentro com ID {} inativado com sucesso.", idHemocentro);
    }

    @Override
    public List<HemocentroPorLocalizacaoResponse> getHemocentroByLocation(Double latitude, Double longitude, Integer raio) {
        log.info("[HemocentroServiceImpl] Iniciando busca de hemocentros por localização e prioridade.");
        List<Hemocentro> nearbyHemocentros = findNearbyHemocentros(latitude, longitude, raio);

        if (nearbyHemocentros.isEmpty()) {
            log.warn("[HemocentroServiceImpl] Nenhum hemocentro encontrado dentro do raio especificado.");
            return List.of();
        }

        UsuarioResponse currentUser = getCurrentUser();
        return sortHemocentrosByPriority(nearbyHemocentros, currentUser);
    }

    private boolean applyHemocentroUpdates(Hemocentro hemocentro, UpdateHemocentroRequest updateHemocentroRequest) {
        boolean hasChanges = false;

        if (updateHemocentroRequest.getTelefone() != null
                && !Objects.equals(hemocentro.getTelefone(), updateHemocentroRequest.getTelefone())) {
            hemocentro.setTelefone(updateHemocentroRequest.getTelefone());
            hasChanges = true;
        }

        if (updateHemocentroRequest.getEmail() != null
                && !Objects.equals(hemocentro.getEmail(), updateHemocentroRequest.getEmail())) {
            hemocentro.setEmail(updateHemocentroRequest.getEmail());
            hasChanges = true;
        }

        if (updateHemocentroRequest.getAtivo() != null
                && !Objects.equals(hemocentro.getAtivo(), updateHemocentroRequest.getAtivo())) {
            hemocentro.setAtivo(updateHemocentroRequest.getAtivo());
            hasChanges = true;
        }

        return hasChanges;
    }

    private EnderecoHemocentro getEnderecoHemocentro(Hemocentro hemocentro) {
        Optional<EnderecoHemocentro> optionalEndereco = enderecoHemocentroRepository.findById(hemocentro.getId());
        if (optionalEndereco.isEmpty()) throw new ResourceNotFoundException("Endereço do hemocentro não encontrado.");
        return optionalEndereco.get();
    }

    private List<Hemocentro> findNearbyHemocentros(Double latitude, Double longitude, Integer raio) {
        Point userLocation = new Point(longitude, latitude);
        Distance distance = new Distance(raio, Metrics.KILOMETERS);
        List<EnderecoHemocentro> enderecoHemocentroList = enderecoHemocentroRepository.findByGeoLocationNear(userLocation, distance);
 
        if (enderecoHemocentroList.isEmpty()) {
            return Collections.emptyList();
        }
 
        List<Integer> nearbyHemocentroIds = enderecoHemocentroList.stream()
                .map(EnderecoHemocentro::getId)
                .toList();
 
        List<Hemocentro> activeHemocentros = repository.getHemocentrosInfoByIds(nearbyHemocentroIds);
 
        Map<Integer, EnderecoHemocentro> enderecoMap = enderecoHemocentroList.stream()
                .collect(Collectors.toMap(EnderecoHemocentro::getId, endereco -> endereco));
 
        return activeHemocentros.stream()
                .peek(hemocentro -> hemocentro.setEndereco(enderecoMap.get(hemocentro.getId())))
                .collect(Collectors.toList());
    }

    private List<HemocentroPorLocalizacaoResponse> sortHemocentrosByPriority(List<Hemocentro> hemocentros, UsuarioResponse usuario) {
        log.info("[HemocentroServiceImpl] Ordenando {} hemocentros por prioridade para o usuário de ID: {}.", hemocentros.size(), usuario.getId());
        List<Integer> hemocentroIdList = hemocentros.stream().map(Hemocentro::getId).toList();
        List<Byte> tipoSanguineoIdList = tipoSanguineoService.getTipoSanguineoCompativel(usuario.getIdTipoSanguineo().byteValue());
        Set<Integer> hemocentrosPrioridadeIdList = repository.getHemocentroIfHasSolicitacaoDoacao(hemocentroIdList, usuario.getId(), tipoSanguineoIdList);

        return hemocentros.stream()
                .map(hemocentro -> {
                    HemocentroPorLocalizacaoResponse response = mapper.toHemocentroPorLocalizacaoResponse(hemocentro);
                    boolean isPrioritario = hemocentrosPrioridadeIdList.contains(hemocentro.getId());
                    response.setPrioridade(isPrioritario);
                    return response;
                })
                .sorted(Comparator.comparing(HemocentroPorLocalizacaoResponse::getPrioridade, Comparator.nullsLast(Boolean::compareTo)).reversed())
                .collect(Collectors.toList());
    }

    private UsuarioResponse getCurrentUser() {
        CustomUserDetails userDetails = AppUtils.getUserDetails();
        return usuarioService.getUserInfoById(userDetails.getIdUsuario());
    }
}