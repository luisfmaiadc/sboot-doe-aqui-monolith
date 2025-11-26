package com.doeaqui.sboot_doe_aqui_monolith.controller;

import com.doeaqui.sboot_doe_aqui_monolith.model.NewUsuarioRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UpdateUsuarioRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UsuarioResponse;
import com.doeaqui.sboot_doe_aqui_monolith.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioApiImplTest {

    @InjectMocks
    private UsuarioApiImpl usuarioApi;

    @Mock
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void postNewUser_shouldReturnCreated() {
        NewUsuarioRequest request = new NewUsuarioRequest();
        UsuarioResponse serviceResponse = new UsuarioResponse().id(1);
        when(usuarioService.postNewUser(any(NewUsuarioRequest.class))).thenReturn(serviceResponse);

        ResponseEntity<UsuarioResponse> response = usuarioApi.postNewUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
        assertEquals(URI.create("http://localhost/1"), response.getHeaders().getLocation());
    }

    @Test
    void getUserInfoById_shouldReturnOk() {
        Integer userId = 1;
        UsuarioResponse serviceResponse = new UsuarioResponse().id(userId).nome("Test User");
        when(usuarioService.getUserInfoById(userId)).thenReturn(serviceResponse);

        ResponseEntity<UsuarioResponse> response = usuarioApi.getUserInfoById(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getId());
        assertEquals("Test User", response.getBody().getNome());
    }

    @Test
    void patchUserInfo_shouldReturnOk() {
        Integer userId = 1;
        UpdateUsuarioRequest request = new UpdateUsuarioRequest();
        UsuarioResponse serviceResponse = new UsuarioResponse().id(userId).nome("Updated User");
        when(usuarioService.patchUserInfo(anyInt(), any(UpdateUsuarioRequest.class))).thenReturn(serviceResponse);

        ResponseEntity<UsuarioResponse> response = usuarioApi.patchUserInfo(userId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getId());
        assertEquals("Updated User", response.getBody().getNome());
    }

    @Test
    void deleteUser_shouldReturnNoContent() {
        Integer userId = 1;
        doNothing().when(usuarioService).deleteUser(userId);

        ResponseEntity<Void> response = usuarioApi.deleteUser(userId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}