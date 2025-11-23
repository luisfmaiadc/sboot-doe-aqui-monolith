package com.doeaqui.sboot_doe_aqui_monolith.service.Impl;

import com.doeaqui.sboot_doe_aqui_monolith.config.exception.ResourceNotFoundException;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Login;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.LoginMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.NewLoginRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UpdateUsuarioRequest;
import com.doeaqui.sboot_doe_aqui_monolith.repository.LoginRepository;
import com.doeaqui.sboot_doe_aqui_monolith.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final LoginRepository repository;
    private final LoginMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void postNewLogin(NewLoginRequest loginRequest, Integer idUsuario) {
        log.info("[LoginServiceImpl] Iniciando cadastro de novo login usuário de ID {}.", idUsuario);
        Login newLogin = mapper.toLogin(loginRequest);
        String senha = passwordEncoder.encode(newLogin.getSenha());
        newLogin.setIdUsuario(idUsuario);
        newLogin.setSenha(senha);
        repository.postNewLogin(newLogin);
        log.info("[LoginServiceImpl] Login do usuário de ID {} cadastrado com sucesso.", idUsuario);
    }

    @Override
    public Login getLoginInfoById(Integer idUsuario) {
        log.info("[LoginServiceImpl] Buscando informações de login do usuário com ID: {}", idUsuario);
        Optional<Login> optionalLogin = repository.getLoginInfoById(idUsuario);
        if (optionalLogin.isEmpty()) throw new ResourceNotFoundException("Nenhuma informação de login foi encontrada.");
        log.info("[LoginServiceImpl] Informações de login do usuário {} encontradas com sucesso.", idUsuario);
        return optionalLogin.get();
    }

    @Override
    @Transactional
    public void patchLoginInfo(Integer idUsuario, UpdateUsuarioRequest updateRequest) {
        log.info("[LoginServiceImpl] Iniciando atualização de informações de login do usuário com ID: {}", idUsuario);
        Login login = getLoginInfoById(idUsuario);
        boolean isUpdateLoginOrPapel = false;

        if (updateRequest.getEmail() != null) {
            login.setEmail(updateRequest.getEmail());
            isUpdateLoginOrPapel = true;
        }

        if (updateRequest.getIdPapel() != null) {
            login.setIdPapel(updateRequest.getIdPapel().byteValue());
            isUpdateLoginOrPapel = true;
        }

        if (isUpdateLoginOrPapel) {
            log.info("[LoginServiceImpl] Atualizando informações de login do usuário {}.", idUsuario);
            repository.patchLoginEmailOrPapel(login);
        }

        if (updateRequest.getSenha() != null) {
            log.info("[LoginServiceImpl] Atualizando senha do usuário {}.", idUsuario);
            String newSenha = passwordEncoder.encode(updateRequest.getSenha());
            login.setSenha(newSenha);
            repository.patchLoginSenha(login);
        }
    }
}