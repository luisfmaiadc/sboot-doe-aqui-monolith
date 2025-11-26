package com.doeaqui.sboot_doe_aqui_monolith.controller;

import com.doeaqui.sboot_doe_aqui_monolith.config.security.CustomUserDetails;
import com.doeaqui.sboot_doe_aqui_monolith.config.security.TokenService;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Login;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.LoginMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.AuthenticationResponse;
import com.doeaqui.sboot_doe_aqui_monolith.model.LoginRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.LoginResponse;
import com.doeaqui.sboot_doe_aqui_monolith.service.LoginService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginApiImplTest {

    @InjectMocks
    private LoginApiImpl loginApi;

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private LoginService loginService;

    @Mock
    private LoginMapper mapper;

    @Test
    void postLoginCredentials_shouldReturnOk() {
        LoginRequest loginRequest = new LoginRequest().email("test@test.com").senha("password");
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getSenha());

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        AuthenticationResponse authResponse = new AuthenticationResponse("token", OffsetDateTime.now());

        when(authenticationManager.authenticate(authenticationToken)).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(tokenService.getToken(userDetails)).thenReturn(authResponse);

        ResponseEntity<AuthenticationResponse> response = loginApi.postLoginCredentials(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("token", response.getBody().getToken());
    }

    @Test
    void getLoginInfoById_shouldReturnOk() {
        Integer idUsuario = 1;
        Login login = new Login();
        login.setIdUsuario(idUsuario);
        login.setEmail("test@test.com");

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setIdUsuario(idUsuario);
        loginResponse.setEmail("test@test.com");

        when(loginService.getLoginInfoById(idUsuario)).thenReturn(login);
        when(mapper.toLoginResponse(any(Login.class))).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = loginApi.getLoginInfoById(idUsuario);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(idUsuario, response.getBody().getIdUsuario());
        assertEquals("test@test.com", response.getBody().getEmail());
    }
}