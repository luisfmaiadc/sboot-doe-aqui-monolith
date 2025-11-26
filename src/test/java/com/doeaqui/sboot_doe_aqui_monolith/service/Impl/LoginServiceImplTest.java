package com.doeaqui.sboot_doe_aqui_monolith.service.Impl;

import com.doeaqui.sboot_doe_aqui_monolith.config.exception.ResourceNotFoundException;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Login;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.LoginMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.NewLoginRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UpdateUsuarioRequest;
import com.doeaqui.sboot_doe_aqui_monolith.repository.LoginRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

    @InjectMocks
    private LoginServiceImpl loginService;

    @Mock
    private LoginRepository repository;

    @Mock
    private LoginMapper mapper;
    
    @Mock
    private PasswordEncoder passwordEncoder;

    private Login login;

    @BeforeEach
    void setUp() {
        login = new Login();
        login.setIdUsuario(1);
        login.setEmail("test@test.com");
        login.setSenha("encodedPassword");
        login.setIdPapel((byte) 2);
    }

    @Nested
    @DisplayName("Tests for postNewLogin")
    class PostNewLogin {
        @Test
        void shouldCreateLoginSuccessfully() {
            login.setSenha("password");
            NewLoginRequest request = new NewLoginRequest().email("new@test.com").senha("password");
            when(mapper.toLogin(request)).thenReturn(login);
            when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

            loginService.postNewLogin(request, 1);

            verify(mapper).toLogin(request);
            verify(passwordEncoder).encode("password");
            verify(repository).postNewLogin(argThat(savedLogin ->
                    savedLogin.getIdUsuario().equals(1) &&
                            savedLogin.getSenha().equals("encodedPassword")
            ));
        }
    }

    @Nested
    @DisplayName("Tests for getLoginInfoById")
    class GetLoginInfoById {
        @Test
        void shouldReturnLogin_whenFound() {
            when(repository.getLoginInfoById(1)).thenReturn(Optional.of(login));

            Login result = loginService.getLoginInfoById(1);

            assertNotNull(result);
            assertEquals(1, result.getIdUsuario());
        }

        @Test
        void shouldThrowNotFoundException_whenNotFound() {
            when(repository.getLoginInfoById(1)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> loginService.getLoginInfoById(1));
        }
    }

    @Nested
    @DisplayName("Tests for patchLoginInfo")
    class PatchLoginInfo {

        @BeforeEach
        void patchSetup() {
            when(repository.getLoginInfoById(anyInt())).thenReturn(Optional.of(login));
        }

        @Test
        void shouldUpdateEmailOnly() {
            UpdateUsuarioRequest request = new UpdateUsuarioRequest().email("new.email@test.com");

            loginService.patchLoginInfo(1, request);

            verify(repository).patchLoginEmailOrPapel(argThat(l -> l.getEmail().equals("new.email@test.com")));
            verify(repository, never()).patchLoginSenha(any());
        }

        @Test
        void shouldUpdatePapelOnly() {
            UpdateUsuarioRequest request = new UpdateUsuarioRequest().idPapel(1);

            loginService.patchLoginInfo(1, request);

            verify(repository).patchLoginEmailOrPapel(argThat(l -> l.getIdPapel() == 1));
            verify(repository, never()).patchLoginSenha(any());
        }

        @Test
        void shouldUpdateSenhaOnly() {
            UpdateUsuarioRequest request = new UpdateUsuarioRequest().senha("newPassword");
            when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

            loginService.patchLoginInfo(1, request);

            verify(repository, never()).patchLoginEmailOrPapel(any());
            verify(passwordEncoder).encode("newPassword");
            verify(repository).patchLoginSenha(argThat(l -> l.getSenha().equals("newEncodedPassword")));
        }

        @Test
        void shouldUpdateAllFields() {
            UpdateUsuarioRequest request = new UpdateUsuarioRequest()
                    .email("new.email@test.com")
                    .idPapel(1)
                    .senha("newPassword");
            when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

            loginService.patchLoginInfo(1, request);

            verify(repository).patchLoginEmailOrPapel(argThat(l ->
                    l.getEmail().equals("new.email@test.com") && l.getIdPapel() == 1
            ));
            verify(passwordEncoder).encode("newPassword");
            verify(repository).patchLoginSenha(argThat(l -> l.getSenha().equals("newEncodedPassword")));
        }

        @Test
        void shouldDoNothing_whenRequestIsEmpty() {
            UpdateUsuarioRequest request = new UpdateUsuarioRequest();

            loginService.patchLoginInfo(1, request);

            verify(repository, never()).patchLoginEmailOrPapel(any());
            verify(repository, never()).patchLoginSenha(any());
            verify(passwordEncoder, never()).encode(any());
        }
    }
}