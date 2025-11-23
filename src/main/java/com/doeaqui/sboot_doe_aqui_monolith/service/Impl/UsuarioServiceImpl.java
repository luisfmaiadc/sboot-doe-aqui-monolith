package com.doeaqui.sboot_doe_aqui_monolith.service.Impl;

import com.doeaqui.sboot_doe_aqui_monolith.config.exception.ResourceNotFoundException;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Papel;
import com.doeaqui.sboot_doe_aqui_monolith.domain.TipoSanguineo;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Usuario;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.UsuarioMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.NewUsuarioRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UpdateUsuarioRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UsuarioResponse;
import com.doeaqui.sboot_doe_aqui_monolith.repository.UsuarioRepository;
import com.doeaqui.sboot_doe_aqui_monolith.service.LoginService;
import com.doeaqui.sboot_doe_aqui_monolith.service.PapelService;
import com.doeaqui.sboot_doe_aqui_monolith.service.TipoSanguineoService;
import com.doeaqui.sboot_doe_aqui_monolith.service.UsuarioService;
import com.doeaqui.sboot_doe_aqui_monolith.util.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final LoginService loginService;
    private final PapelService papelService;
    private final TipoSanguineoService tipoSanguineoService;

    private static final Set<String> supportedGendersSet = Set.of("M", "F", "O");

    @Override
    @Transactional
    public UsuarioResponse postNewUser(NewUsuarioRequest usuarioRequest) {
        log.info("[UsuarioServiceImpl] Iniciando cadastro de novo usuário.");
        validateNewUserRequest(usuarioRequest);
        Usuario newUser = usuarioMapper.toUsuario(usuarioRequest);
        newUser.setAtivo(Boolean.TRUE);
        int generatedId = usuarioRepository.postNewUser(newUser);
        loginService.postNewLogin(usuarioRequest.getLogin(), generatedId);
        log.info("[UsuarioServiceImpl] Usuário cadastrado com sucesso. ID gerado: {}.", generatedId);
        return getUserInfoById(generatedId);
    }

    @Override
    public UsuarioResponse getUserInfoById(Integer id) {
        log.info("[UsuarioServiceImpl] Buscando informações do usuário com ID: {}", id);
        Optional<UsuarioResponse> optionalUsuario = usuarioRepository.getUserInfoById(id);
        if (optionalUsuario.isEmpty()) throw new ResourceNotFoundException("Nenhuma informação do usuário foi encontrada.");
        log.info("[UsuarioServiceImpl] Informações do usuário {} encontradas com sucesso.", id);
        return optionalUsuario.get();
    }

    @Override
    @Transactional
    public UsuarioResponse patchUserInfo(Integer idUsuario, UpdateUsuarioRequest updateRequest) {
        log.info("[UsuarioServiceImpl] Iniciando atualização de informações do usuário com ID: {}", idUsuario);
        AppUtils.requireAtLeastOneNonNull(Arrays.asList(updateRequest.getEmail(), updateRequest.getSenha(), updateRequest.getGenero(),
                updateRequest.getTelefone(), updateRequest.getIdPapel()));

        UsuarioResponse usuario = getUserInfoById(idUsuario);
        Usuario userInfo = usuarioMapper.toUsuarioFromResponse(usuario);

        boolean userFieldsChanged = applyUserUpdates(updateRequest, userInfo);
        boolean loginFieldsChanged = isUpdateLoginValid(updateRequest, usuario);

        if (!userFieldsChanged && !loginFieldsChanged)
            throw new IllegalArgumentException("Informe ao menos um campo para atualizar ou os valores informados são os mesmos dos atuais.");

        if (userFieldsChanged) {
            log.info("[UsuarioServiceImpl] Atualizando informações do usuário {}.", idUsuario);
            usuarioRepository.patchUserInfo(userInfo);
        }

        if (loginFieldsChanged) {
            log.info("[UsuarioServiceImpl] Atualizando informações de login do usuário {}.", idUsuario);
            loginService.patchLoginInfo(idUsuario, updateRequest);
        }

        return getUserInfoById(idUsuario);
    }

    @Override
    @Transactional
    public void deleteUser(Integer idUsuario) {
        log.info("[UsuarioServiceImpl] Inativando usuário com ID: {}", idUsuario);
        UsuarioResponse usuario = getUserInfoById(idUsuario);
        if (Objects.equals(usuario.getAtivo(), Boolean.FALSE)) throw new IllegalArgumentException("Usuário já desativado.");
        usuarioRepository.deleteUser(idUsuario);
        log.info("[UsuarioServiceImpl] Usuário com ID: {} inativado com sucesso.", idUsuario);
    }

    private void validateNewUserRequest(NewUsuarioRequest usuarioRequest) {
        validadeGenders(usuarioRequest.getGenero());
        validateCpf(usuarioRequest.getCpf());
        validateTipoSanguineo(usuarioRequest.getIdTipoSanguineo());
        validatePapel(usuarioRequest.getLogin().getIdPapel());
        validateIdade(usuarioRequest.getLogin().getIdPapel(), usuarioRequest.getDataNascimento());
    }

    private void validateIdade(Integer idPapel, LocalDate dataNascimento) {
        Papel papel = papelService.getPapelById(idPapel);
        if (!papel.getNome().equals("PACIENTE")) {
            int idade = Period.between(dataNascimento, LocalDate.now()).getYears();
            if (idade < 16) throw new IllegalArgumentException("É necessário ter no mínimo 16 anos para se cadastrar como doador.");
        }
    }

    private void validateTipoSanguineo(Integer idTipoSanguineo) {
        List<TipoSanguineo> tipoSanguineoList = tipoSanguineoService.getTiposSanguineos();
        if (tipoSanguineoList.stream().noneMatch(t -> t.getId() == idTipoSanguineo.byteValue()))
            throw new IllegalArgumentException("Tipo sanguíneo informado inválido.");
    }

    private void validatePapel(Integer idPapel) {
        Papel papel = papelService.getPapelById(idPapel);
        if (papel.getNome().equals("ADMIN")) throw new IllegalArgumentException("Papel inválido.");
    }

    private void validadeGenders(String gender) {
        if (!supportedGendersSet.contains(gender))
            throw new IllegalArgumentException("Gênero informado não suportado.");
    }

    private void validateCpf(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        try {
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += (cpf.charAt(i) - '0') * (10 - i);
            }
            int firstVerifier = 11 - (sum % 11);
            if (firstVerifier >= 10) {
                firstVerifier = 0;
            }

            if ((cpf.charAt(9) - '0') != firstVerifier) {
                throw new IllegalArgumentException("CPF inválido.");
            }

            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += (cpf.charAt(i) - '0') * (11 - i);
            }
            int secondVerifier = 11 - (sum % 11);
            if (secondVerifier >= 10) {
                secondVerifier = 0;
            }

            if ((cpf.charAt(10) - '0') != secondVerifier) {
                throw new IllegalArgumentException("CPF inválido.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("CPF deve conter apenas números.");
        }
    }

    private boolean applyUserUpdates(UpdateUsuarioRequest request, Usuario usuarioToUpdate) {
        boolean hasChanges = false;

        if (request.getGenero() != null && !Objects.equals(usuarioToUpdate.getGenero(), request.getGenero().charAt(0))) {
            validadeGenders(request.getGenero());
            usuarioToUpdate.setGenero(request.getGenero().charAt(0));
            hasChanges = true;
        }

        if (request.getTelefone() != null && !Objects.equals(usuarioToUpdate.getTelefone(), request.getTelefone())) {
            usuarioToUpdate.setTelefone(request.getTelefone());
            hasChanges = true;
        }

        return hasChanges;
    }

    private boolean isUpdateLoginValid(UpdateUsuarioRequest request, UsuarioResponse usuario) {
        boolean isUpdateLoginValid = false;

        if (request.getEmail() != null && !Objects.equals(request.getEmail(), usuario.getEmail())) {
            isUpdateLoginValid = true;
        }

        if (request.getSenha() != null) {
            isUpdateLoginValid = true;
        }

        if (request.getIdPapel() != null && !Objects.equals(usuario.getIdPapel(), request.getIdPapel())) {
            validatePapel(request.getIdPapel());
            isUpdateLoginValid = true;
        }

        return isUpdateLoginValid;
    }
}