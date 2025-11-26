package com.doeaqui.sboot_doe_aqui_monolith.service.Impl;

import com.doeaqui.sboot_doe_aqui_monolith.config.exception.ResourceNotFoundException;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Papel;
import com.doeaqui.sboot_doe_aqui_monolith.domain.TipoSanguineo;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Usuario;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.UsuarioMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.*;
import com.doeaqui.sboot_doe_aqui_monolith.repository.UsuarioRepository;
import com.doeaqui.sboot_doe_aqui_monolith.service.LoginService;
import com.doeaqui.sboot_doe_aqui_monolith.service.PapelService;
import com.doeaqui.sboot_doe_aqui_monolith.service.TipoSanguineoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private LoginService loginService;

    @Mock
    private PapelService papelService;
    
    @Mock
    private TipoSanguineoService tipoSanguineoService;

    private NewUsuarioRequest newUsuarioRequest;
    private Usuario usuario;
    private UsuarioResponse usuarioResponse;
    private UpdateUsuarioRequest updateUsuarioRequest;

    @BeforeEach
    void setUp() {
        NewLoginRequest newLoginRequest = new NewLoginRequest()
                .email("test@test.com")
                .senha("password")
                .idPapel(2);

        newUsuarioRequest = new NewUsuarioRequest()
                .nome("Test User")
                .cpf("53046961588")
                .dataNascimento(LocalDate.of(1990, 1, 1))
                .genero("M")
                .telefone("11999999999")
                .idTipoSanguineo(1)
                .login(newLoginRequest);

        usuario = new Usuario();
        usuario.setId(1);

        usuarioResponse = new UsuarioResponse()
                .id(1)
                .nome("Test User")
                .email("test@test.com")
                .ativo(true)
                .idPapel(2);

        updateUsuarioRequest = new UpdateUsuarioRequest();
    }

    @Nested
    @DisplayName("Tests for postNewUser")
    class PostNewUser {
        @Test
        void postNewUser_shouldCreateUserSuccessfully() {
            when(papelService.getPapelById(anyInt())).thenReturn(new Papel((byte) 2, "DOADOR")); 
            when(tipoSanguineoService.getTiposSanguineos()).thenReturn(Collections.singletonList(new TipoSanguineo((byte) 1, "A", '+')));
            when(usuarioMapper.toUsuario(any(NewUsuarioRequest.class))).thenReturn(usuario);
            when(usuarioRepository.postNewUser(any(Usuario.class))).thenReturn(1);
            when(usuarioRepository.getUserInfoById(1)).thenReturn(Optional.of(usuarioResponse));

            UsuarioResponse result = usuarioService.postNewUser(newUsuarioRequest);

            assertNotNull(result);
            assertEquals(1, result.getId());
            verify(loginService).postNewLogin(newUsuarioRequest.getLogin(), 1);
        }

        @Test
        void postNewUser_shouldThrowExceptionForInvalidAge() {
            newUsuarioRequest.setDataNascimento(LocalDate.now().minusYears(15));
            when(tipoSanguineoService.getTiposSanguineos()).thenReturn(Collections.singletonList(new TipoSanguineo((byte) 1, "A", '+')));
            when(papelService.getPapelById(anyInt())).thenReturn(new Papel((byte) 2, "DOADOR"));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.postNewUser(newUsuarioRequest));

            assertEquals("É necessário ter no mínimo 16 anos para se cadastrar como doador.", exception.getMessage());
        }

        @Test
        void postNewUser_shouldThrowExceptionForInvalidCpf() {
            newUsuarioRequest.setCpf("11111111111");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.postNewUser(newUsuarioRequest));

            assertEquals("CPF inválido.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests for getUserInfoById")
    class GetUserInfoById {
        @Test
        void getUserInfoById_shouldReturnUser() {
            when(usuarioRepository.getUserInfoById(1)).thenReturn(Optional.of(usuarioResponse));

            UsuarioResponse result = usuarioService.getUserInfoById(1);

            assertNotNull(result);
            assertEquals(1, result.getId());
        }

        @Test
        void getUserInfoById_shouldThrowNotFoundException() {
            when(usuarioRepository.getUserInfoById(1)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> usuarioService.getUserInfoById(1));

            assertEquals("Nenhuma informação do usuário foi encontrada.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests for patchUserInfo")
    class PatchUserInfo {

        @Test
        void patchUserInfo_shouldUpdateUserFieldsOnly() {
            when(usuarioRepository.getUserInfoById(1)).thenReturn(Optional.of(usuarioResponse));
            updateUsuarioRequest.setTelefone("22888888888");
            when(usuarioMapper.toUsuarioFromResponse(any(UsuarioResponse.class))).thenReturn(usuario);

            usuarioService.patchUserInfo(1, updateUsuarioRequest);

            verify(usuarioRepository).patchUserInfo(any(Usuario.class));
            verify(loginService, never()).patchLoginInfo(anyInt(), any(UpdateUsuarioRequest.class));
        }

        @Test
        void patchUserInfo_shouldUpdateLoginFieldsOnly() {
            when(usuarioRepository.getUserInfoById(1)).thenReturn(Optional.of(usuarioResponse));
            updateUsuarioRequest.setEmail("newemail@test.com");
            when(usuarioMapper.toUsuarioFromResponse(any(UsuarioResponse.class))).thenReturn(usuario);

            usuarioService.patchUserInfo(1, updateUsuarioRequest);

            verify(usuarioRepository, never()).patchUserInfo(any(Usuario.class));
            verify(loginService).patchLoginInfo(1, updateUsuarioRequest);
        }

        @Test
        void patchUserInfo_shouldUpdateBothUserAndLoginFields() {
            when(usuarioRepository.getUserInfoById(1)).thenReturn(Optional.of(usuarioResponse));
            updateUsuarioRequest.setTelefone("22888888888");
            updateUsuarioRequest.setEmail("newemail@test.com");
            when(usuarioMapper.toUsuarioFromResponse(any(UsuarioResponse.class))).thenReturn(usuario);

            usuarioService.patchUserInfo(1, updateUsuarioRequest);

            verify(usuarioRepository).patchUserInfo(any(Usuario.class));
            verify(loginService).patchLoginInfo(1, updateUsuarioRequest);
        }

        @Test
        void patchUserInfo_shouldThrowExceptionForNoChanges() {
            when(usuarioRepository.getUserInfoById(1)).thenReturn(Optional.of(usuarioResponse));
            updateUsuarioRequest.setEmail("test@test.com"); // Same email
            when(usuarioMapper.toUsuarioFromResponse(any(UsuarioResponse.class))).thenReturn(usuario);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.patchUserInfo(1, updateUsuarioRequest));

            assertEquals("Informe ao menos um campo para atualizar ou os valores informados são os mesmos dos atuais.", exception.getMessage());
        }

        @Test
        void patchUserInfo_shouldThrowExceptionForNoFields() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.patchUserInfo(1, new UpdateUsuarioRequest()));

            assertEquals("Algum parâmetro deve ser informado na requisição.", exception.getMessage());
        }

        @Test
        void patchUserInfo_shouldThrowExceptionForInvalidPapel() {
            when(usuarioRepository.getUserInfoById(1)).thenReturn(Optional.of(usuarioResponse));
            updateUsuarioRequest.setIdPapel(1); // ADMIN
            when(usuarioMapper.toUsuarioFromResponse(any(UsuarioResponse.class))).thenReturn(usuario);
            when(papelService.getPapelById(1)).thenReturn(new Papel((byte) 1, "ADMIN"));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.patchUserInfo(1, updateUsuarioRequest));

            assertEquals("Papel inválido.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests for deleteUser")
    class DeleteUser {
        @Test
        void deleteUser_shouldDeactivateUserSuccessfully() {
            when(usuarioRepository.getUserInfoById(1)).thenReturn(Optional.of(usuarioResponse));

            usuarioService.deleteUser(1);

            verify(usuarioRepository).deleteUser(1);
        }

        @Test
        void deleteUser_shouldThrowExceptionForAlreadyInactiveUser() {
            usuarioResponse.setAtivo(false);
            when(usuarioRepository.getUserInfoById(1)).thenReturn(Optional.of(usuarioResponse));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.deleteUser(1));

            assertEquals("Usuário já desativado.", exception.getMessage());
        }

        @Test
        void deleteUser_shouldThrowNotFoundException() {
            when(usuarioRepository.getUserInfoById(1)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> usuarioService.deleteUser(1));

            assertEquals("Nenhuma informação do usuário foi encontrada.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests for private validations")
    class PrivateValidations {

        @Test
        void validateNewUserRequest_shouldThrowExceptionForInvalidGender() {
            newUsuarioRequest.setCpf("12345678901");
            newUsuarioRequest.setGenero("X");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.postNewUser(newUsuarioRequest));

            assertEquals("Gênero informado não suportado.", exception.getMessage());
        }

        @Test
        void validateNewUserRequest_shouldThrowExceptionForInvalidBloodType() {
            when(tipoSanguineoService.getTiposSanguineos()).thenReturn(List.of(new TipoSanguineo((byte) 2, "B", '+')));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.postNewUser(newUsuarioRequest));

            assertEquals("Tipo sanguíneo informado inválido.", exception.getMessage());
        }

        @Test
        void validateNewUserRequest_shouldThrowExceptionForInvalidPapel() {
            when(papelService.getPapelById(anyInt())).thenReturn(new Papel((byte) 1, "ADMIN"));
            when(tipoSanguineoService.getTiposSanguineos()).thenReturn(Collections.singletonList(new TipoSanguineo((byte) 1, "A", '+')));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.postNewUser(newUsuarioRequest));

            assertEquals("Papel inválido.", exception.getMessage());
        }
    }
}