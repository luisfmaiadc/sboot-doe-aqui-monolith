package com.doeaqui.sboot_doe_aqui_monolith.service.Impl;

import com.doeaqui.sboot_doe_aqui_monolith.config.exception.ResourceNotFoundException;
import com.doeaqui.sboot_doe_aqui_monolith.config.security.CustomUserDetails;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Doacao;
import com.doeaqui.sboot_doe_aqui_monolith.domain.SolicitacaoDoacao;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.DoacaoMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.NewDoacaoRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UpdateDoacaoRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UsuarioResponse;
import com.doeaqui.sboot_doe_aqui_monolith.repository.DoacaoRepository;
import com.doeaqui.sboot_doe_aqui_monolith.service.*;
import com.doeaqui.sboot_doe_aqui_monolith.util.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoacaoServiceImpl implements DoacaoService {

    private final HemocentroService hemocentroService;
    private final UsuarioService usuarioService;
    private final SolicitacaoDoacaoService solicitacaoDoacaoService;
    private final TipoSanguineoService tipoSanguineoService;
    private final DoacaoRepository repository;
    private final DoacaoMapper mapper;

    @Override
    @Transactional
    public Doacao postNewDoacao(NewDoacaoRequest doacaoRequest) {
        log.info("[DoacaoServiceImpl] Iniciando cadastro de nova doação.");
        Doacao newDoacao = mapper.toDoacao(doacaoRequest);
        CustomUserDetails userDetails = AppUtils.getUserDetails();
        UsuarioResponse usuario = usuarioService.getUserInfoById(userDetails.getIdUsuario());
        isNewDoacaoValid(newDoacao, usuario);
        newDoacao.setIdUsuario(userDetails.getIdUsuario());
        newDoacao.setDataDoacao(LocalDateTime.now());
        int generatedId = repository.postNewDoacao(newDoacao);
        newDoacao.setId(generatedId);
        log.info("[DoacaoServiceImpl] Nova doação cadastrada com sucesso para o usuário {}. ID gerado da doação: {}.", usuario.getId(), generatedId);
        return getDoacaoInfoById(generatedId);
    }

    @Override
    public Doacao getDoacaoInfoById(Integer idDoacao) {
        log.info("[DoacaoServiceImpl] Buscando informações da doação com ID {}.", idDoacao);
        Optional<Doacao> optionalDoacao = repository.getDoacaoInfoById(idDoacao);
        if (optionalDoacao.isEmpty()) throw new ResourceNotFoundException("Doação não encontrada.");
        CustomUserDetails userDetails = AppUtils.getUserDetails();
        Doacao doacao = optionalDoacao.get();
        boolean isAdmin = AppUtils.isAdmin();
        boolean isOwner = doacao.getIdUsuario().equals(userDetails.getIdUsuario());
        if (!isOwner && !isAdmin) throw new AuthorizationDeniedException("Acesso negado.");
        log.info("[DoacaoServiceImpl] Informações da doação com ID {} encontradas com sucesso.", idDoacao);
        return doacao;
    }

    @Override
    public List<Doacao> getDoacaoByFilter(Integer idUsuario, Integer idHemocentro, LocalDate dataDoacao, Integer volume) {
        log.info("[DoacaoServiceImpl] Iniciando busca de doações por filtros.");
        AppUtils.requireAtLeastOneNonNull(Arrays.asList(idUsuario, idHemocentro, dataDoacao, volume));
        CustomUserDetails userDetails = AppUtils.getUserDetails();
        boolean isAdmin = AppUtils.isAdmin();
        if (!isAdmin && idUsuario != null) throw new AuthorizationDeniedException("Apenas um administrador pode visualizar doações de outro usuário.");
        List<Doacao> doacaoList = repository.getDoacaoByFilter(idHemocentro, dataDoacao, volume, isAdmin ? idUsuario : userDetails.getIdUsuario());
        if (doacaoList.isEmpty()) {
            log.warn("[DoacaoServiceImpl] Nenhuma doação encontrada com os filtros informados.");
            return doacaoList;
        }
        log.info("[DoacaoServiceImpl] {} Doações encontradas com os filtros informados.", doacaoList.size());
        return doacaoList;
    }

    @Override
    @Transactional
    public Doacao patchDoacaoInfo(Integer idDoacao, UpdateDoacaoRequest updateDoacaoRequest) {
        log.info("[DoacaoServiceImpl] Iniciando atualização de informações da doação com ID {}.", idDoacao);
        AppUtils.requireAtLeastOneNonNull(Collections.singletonList(updateDoacaoRequest.getObservacoes()));
        Doacao doacao = getDoacaoInfoById(idDoacao);
        CustomUserDetails userDetails = AppUtils.getUserDetails();
        boolean isAdmin = AppUtils.isAdmin();
        boolean isOwner = doacao.getIdUsuario().equals(userDetails.getIdUsuario());
        if (!isOwner && !isAdmin) throw new AuthorizationDeniedException("Apenas um administrador pode alterar uma doação de outro usuário.");
        doacao.setObservacoes(updateDoacaoRequest.getObservacoes());
        repository.patchDoacaoInfo(doacao);
        log.info("[DoacaoServiceImpl] Informações da doação com ID {} atualizadas com sucesso.", idDoacao);
        return getDoacaoInfoById(idDoacao);
    }

    private void isNewDoacaoValid(Doacao doacao, UsuarioResponse usuario) {
        if (doacao.getVolume() != null && (doacao.getVolume() < 0 || doacao.getVolume() > 500)) {
            throw new IllegalArgumentException("Volume de sangue da doação é inválido.");
        }

        if (doacao.getIdSolicitacaoDoacao() != null) {
            SolicitacaoDoacao solicitacaoDoacao = solicitacaoDoacaoService.getSolicitacaoDoacaoInfoById(doacao.getIdSolicitacaoDoacao());
            tipoSanguineoService.validateBloodCompatible(solicitacaoDoacao.getIdTipoSanguineo(), usuario.getIdTipoSanguineo().byteValue());
        }

        repository.getUltimaDoacao(usuario.getId()).ifPresent(ultimaDoacao -> {
            int mesesDeEspera = "M".equals(usuario.getGenero()) ? 2 : 3;
            LocalDateTime dataMinimaParaDoar = LocalDateTime.now().minusMonths(mesesDeEspera);

            if (ultimaDoacao.getDataDoacao().isAfter(dataMinimaParaDoar)) {
                throw new IllegalArgumentException("Intervalo mínimo entre doações não atingido. Aguarde pelo o menos " + mesesDeEspera + " meses desde a última doação para realizar uma nova.");
            }
        });

        hemocentroService.getHemocentroInfoById(doacao.getIdHemocentro());
    }
}