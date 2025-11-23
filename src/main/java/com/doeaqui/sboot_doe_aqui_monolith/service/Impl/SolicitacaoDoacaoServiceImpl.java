package com.doeaqui.sboot_doe_aqui_monolith.service.Impl;

import com.doeaqui.sboot_doe_aqui_monolith.config.exception.ResourceNotFoundException;
import com.doeaqui.sboot_doe_aqui_monolith.config.security.CustomUserDetails;
import com.doeaqui.sboot_doe_aqui_monolith.domain.SolicitacaoDoacao;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Status;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.SolicitacaoDoacaoMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.NewSolicitacaoDoacaoRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UpdateSolicitacaoDoacaoRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UsuarioResponse;
import com.doeaqui.sboot_doe_aqui_monolith.repository.SolicitacaoRepository;
import com.doeaqui.sboot_doe_aqui_monolith.service.HemocentroService;
import com.doeaqui.sboot_doe_aqui_monolith.service.SolicitacaoDoacaoService;
import com.doeaqui.sboot_doe_aqui_monolith.service.UsuarioService;
import com.doeaqui.sboot_doe_aqui_monolith.util.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitacaoDoacaoServiceImpl implements SolicitacaoDoacaoService {

    private final SolicitacaoRepository repository;
    private final SolicitacaoDoacaoMapper mapper;
    private final HemocentroService hemocentroService;
    private final UsuarioService usuarioService;

    @Override
    @Transactional
    public SolicitacaoDoacao postNewSolicitacaoDoacao(NewSolicitacaoDoacaoRequest request) {
        log.info("[SolicitacaoDoacaoServiceImpl] Iniciando cadastro de nova solicitação de doação.");
        CustomUserDetails userDetails = AppUtils.getUserDetails();
        boolean isAdmin = AppUtils.isAdmin();

        if (!isAdmin && !Objects.equals(request.getIdUsuario(), userDetails.getIdUsuario())) {
            throw new AuthorizationDeniedException("Usuário não tem permissão para criar uma solicitação para outro usuário.");
        }

        boolean isPacienteOuDoadorPaciente = userDetails.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_PACIENTE") || ga.getAuthority().equals("ROLE_DOADOR_PACIENTE"));

        isSolicitacaoDoacaoValid(request);
        SolicitacaoDoacao newSolicitacaoDoacao = mapper.toSolicitacaoDoacao(request);

        if (isPacienteOuDoadorPaciente) {
            UsuarioResponse usuario = usuarioService.getUserInfoById(userDetails.getIdUsuario());
            newSolicitacaoDoacao.setIdTipoSanguineo(usuario.getIdTipoSanguineo().byteValue());
            log.info("[SolicitacaoDoacaoServiceImpl] Cadastrando solicitação de doação para o usuário logado de ID {}.", userDetails.getIdUsuario());
        } else {
            UsuarioResponse usuario = usuarioService.getUserInfoById(request.getIdUsuario());
            newSolicitacaoDoacao.setIdTipoSanguineo(usuario.getIdTipoSanguineo().byteValue());
            log.info("[SolicitacaoDoacaoServiceImpl] Cadastrando solicitação de doação para o usuário informado na requisição de ID {}.", newSolicitacaoDoacao.getIdUsuario());
        }

        newSolicitacaoDoacao.setDataSolicitacao(LocalDateTime.now());
        newSolicitacaoDoacao.setStatus(Status.ABERTA);
        int idSolicitacaoDoacao = repository.postNewSolicitacaoDoacao(newSolicitacaoDoacao);
        newSolicitacaoDoacao.setId(idSolicitacaoDoacao);
        log.info("[SolicitacaoDoacaoServiceImpl] Nova solicitação de doação cadastrada com sucesso para o usuário {}. ID gerado da solicitação: {}.", newSolicitacaoDoacao.getIdUsuario(), idSolicitacaoDoacao);
        return newSolicitacaoDoacao;
    }

    @Override
    public List<SolicitacaoDoacao> getSolicitacaoDoacaoByFilter(Integer idUsuario, Integer idHemocentro, Integer idTipoSanguineo, LocalDate dataSolicitacao, String status, LocalDate dataEncerramento) {
        log.info("[SolicitacaoDoacaoServiceImpl] Iniciando busca de solicitações de doação por filtros.");
        AppUtils.requireAtLeastOneNonNull(Arrays.asList(idUsuario, idHemocentro, idTipoSanguineo, dataSolicitacao, status, dataEncerramento));
        List<SolicitacaoDoacao> solicitacaoDoacaoList = repository.getSolicitacaoDoacaoByFilter(idUsuario, idHemocentro, idTipoSanguineo, dataSolicitacao, status, dataEncerramento);
        if (solicitacaoDoacaoList.isEmpty()) {
            log.warn("[SolicitacaoDoacaoServiceImpl] Nenhuma solicitação de doação encontrada com os filtros informados.");
            return solicitacaoDoacaoList;
        }
        log.info("[SolicitacaoDoacaoServiceImpl] {} Solicitações de doação encontradas com os filtros informados.", solicitacaoDoacaoList.size());
        return solicitacaoDoacaoList;
    }

    @Override
    public SolicitacaoDoacao getSolicitacaoDoacaoInfoById(Integer idSolicitacaoDoacao) {
        log.info("[SolicitacaoDoacaoServiceImpl] Buscando informações da solicitação de doação com ID {}.", idSolicitacaoDoacao);
        Optional<SolicitacaoDoacao> optionalSolicitacaoDoacao = repository.getSolicitacaoDoacaoInfoById(idSolicitacaoDoacao);
        if (optionalSolicitacaoDoacao.isEmpty()) throw new ResourceNotFoundException("Solicitação de doação não encontrada.");
        log.info("[SolicitacaoDoacaoServiceImpl] Informações da solicitação de doação com ID {} encontradas com sucesso.", idSolicitacaoDoacao);
        return optionalSolicitacaoDoacao.get();
    }

    @Override
    @Transactional
    public SolicitacaoDoacao patchSolicitacaoDoacaoInfo(Integer idSolicitacaoDoacao, UpdateSolicitacaoDoacaoRequest updateSolicitacaoRequest) {
        log.info("[SolicitacaoDoacaoServiceImpl] Iniciando atualização de informações da solicitação de doação com ID {}.", idSolicitacaoDoacao);
        AppUtils.requireAtLeastOneNonNull(Arrays.asList(updateSolicitacaoRequest.getStatus(), updateSolicitacaoRequest.getObservacoes()));
        SolicitacaoDoacao solicitacaoDoacao = getSolicitacaoDoacaoInfoById(idSolicitacaoDoacao);
        CustomUserDetails userDetails = AppUtils.getUserDetails();

        boolean isAdmin = AppUtils.isAdmin();
        boolean isOwner = solicitacaoDoacao.getIdUsuario().equals(userDetails.getIdUsuario());

        if (!isAdmin && !isOwner) throw new AccessDeniedException("Acesso negado.");

        if (solicitacaoDoacao.getStatus().equals(Status.ENCERRADA) || solicitacaoDoacao.getStatus().equals(Status.CANCELADA))
            throw new IllegalArgumentException("Não é possível atualizar uma solicitação após seu cancelamento ou encerramento.");

        boolean solicitacaoDoacaoFieldsChanged = applySolicitacaoDoacaoUpdates(updateSolicitacaoRequest, solicitacaoDoacao);
        if (!solicitacaoDoacaoFieldsChanged) throw new IllegalArgumentException("Informe ao menos um campo para atualizar.");

        repository.patchSolicitacaoDoacaoInfo(solicitacaoDoacao);
        log.info("[SolicitacaoDoacaoServiceImpl] Informações da solicitação de doação com ID {} atualizadas com sucesso.", idSolicitacaoDoacao);
        return getSolicitacaoDoacaoInfoById(idSolicitacaoDoacao);
    }

    private boolean applySolicitacaoDoacaoUpdates(UpdateSolicitacaoDoacaoRequest updateSolicitacaoRequest, SolicitacaoDoacao solicitacaoDoacao) {
        boolean hasChanges = false;

        if (updateSolicitacaoRequest.getStatus() != null) {
            Status newStatus = validateAndGetStatus(updateSolicitacaoRequest.getStatus());

            if (!Objects.equals(newStatus, solicitacaoDoacao.getStatus())) {
                if (newStatus.equals(Status.ABERTA) && solicitacaoDoacao.getStatus().equals(Status.EM_ANDAMENTO)) {
                    throw new IllegalArgumentException("Não é possível retornar o status de 'EM_ANDAMENTO' para 'ABERTA'.");
                }

                if (newStatus.equals(Status.ENCERRADA) || newStatus.equals(Status.CANCELADA)) {
                    solicitacaoDoacao.setDataEncerramento(LocalDateTime.now());
                }

                solicitacaoDoacao.setStatus(newStatus);
                hasChanges = true;
            }
        }

        if (updateSolicitacaoRequest.getObservacoes() != null && !Objects.equals(solicitacaoDoacao.getObservacoes(), updateSolicitacaoRequest.getObservacoes())) {
            solicitacaoDoacao.setObservacoes(updateSolicitacaoRequest.getObservacoes());
            hasChanges = true;
        }

        return hasChanges;
    }

    private Status validateAndGetStatus(String statusStr) {
        return Stream.of(Status.values())
                .filter(s -> s.name().equalsIgnoreCase(statusStr))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Status informado inválido: " + statusStr + "."));
    }

    private void isSolicitacaoDoacaoValid(NewSolicitacaoDoacaoRequest solicitacaoDoacaoRequest) {
        boolean isNotValid = repository.isSolicitacaoDoacaoValid(solicitacaoDoacaoRequest.getIdUsuario());
        if (isNotValid) throw new IllegalArgumentException("Já existe uma solicitação de doação em curso para este usuário.");
        hemocentroService.getHemocentroInfoById(solicitacaoDoacaoRequest.getIdHemocentro());
    }
}