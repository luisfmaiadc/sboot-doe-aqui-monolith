package com.doeaqui.sboot_doe_aqui_monolith.service.Impl;

import com.doeaqui.sboot_doe_aqui_monolith.config.exception.ResourceNotFoundException;
import com.doeaqui.sboot_doe_aqui_monolith.config.security.CustomUserDetails;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Login;
import com.doeaqui.sboot_doe_aqui_monolith.domain.SolicitacaoDoacao;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Status;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.SolicitacaoDoacaoMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.NewSolicitacaoDoacaoRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UpdateSolicitacaoDoacaoRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UsuarioResponse;
import com.doeaqui.sboot_doe_aqui_monolith.repository.SolicitacaoRepository;
import com.doeaqui.sboot_doe_aqui_monolith.service.HemocentroService;
import com.doeaqui.sboot_doe_aqui_monolith.service.UsuarioService;
import com.doeaqui.sboot_doe_aqui_monolith.util.AppUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitacaoDoacaoServiceImplTest {

    @InjectMocks
    private SolicitacaoDoacaoServiceImpl solicitacaoService;

    @Mock
    private SolicitacaoRepository repository;

    @Mock
    private SolicitacaoDoacaoMapper mapper;

    @Mock
    private HemocentroService hemocentroService;

    @Mock
    private UsuarioService usuarioService;

    private CustomUserDetails userDetails;
    private SolicitacaoDoacao solicitacao;
    private NewSolicitacaoDoacaoRequest newRequest;

    @BeforeEach
    void setUp() {
        Login login = new Login(1, "paciente@test.com", "pass", (byte) 3, (byte) 0); // ROLE_PACIENTE
        userDetails = new CustomUserDetails(login, List.of(new SimpleGrantedAuthority("ROLE_PACIENTE")));

        solicitacao = new SolicitacaoDoacao();
        solicitacao.setId(1);
        solicitacao.setIdUsuario(1);
        solicitacao.setStatus(Status.ABERTA);

        newRequest = new NewSolicitacaoDoacaoRequest().idUsuario(1).idHemocentro(1);
    }

    @Nested
    @DisplayName("Tests for postNewSolicitacaoDoacao")
    class PostNewSolicitacaoDoacao {
        @Test
        void shouldCreateSuccessfully_forPaciente() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                mockedAppUtils.when(AppUtils::isAdmin).thenReturn(false);

                when(repository.isSolicitacaoDoacaoValid(1)).thenReturn(false);
                when(mapper.toSolicitacaoDoacao(newRequest)).thenReturn(solicitacao);
                when(usuarioService.getUserInfoById(1)).thenReturn(new UsuarioResponse().idTipoSanguineo(1));
                when(repository.postNewSolicitacaoDoacao(solicitacao)).thenReturn(1);

                SolicitacaoDoacao result = solicitacaoService.postNewSolicitacaoDoacao(newRequest);

                assertNotNull(result);
                assertEquals(1, result.getId());
                assertEquals(Status.ABERTA, result.getStatus());
                verify(hemocentroService).getHemocentroInfoById(1);
            }
        }

        @Test
        void shouldCreateSuccessfully_byAdmin_forOtherUser() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                Login adminLogin = new Login(99, "admin@test.com", "pass", (byte) 1, (byte) 0);
                CustomUserDetails adminDetails = new CustomUserDetails(adminLogin, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(adminDetails);
                mockedAppUtils.when(AppUtils::isAdmin).thenReturn(true);

                newRequest.setIdUsuario(2);
                solicitacao.setIdUsuario(2);

                when(repository.isSolicitacaoDoacaoValid(2)).thenReturn(false);
                when(mapper.toSolicitacaoDoacao(newRequest)).thenReturn(solicitacao);
                when(usuarioService.getUserInfoById(2)).thenReturn(new UsuarioResponse().idTipoSanguineo(2));
                when(repository.postNewSolicitacaoDoacao(solicitacao)).thenReturn(1);

                SolicitacaoDoacao result = solicitacaoService.postNewSolicitacaoDoacao(newRequest);

                assertNotNull(result);
                assertEquals(2, result.getIdUsuario());
            }
        }

        @Test
        void shouldThrowAuthorizationDenied_whenUserCreatesForAnother() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                mockedAppUtils.when(AppUtils::isAdmin).thenReturn(false);

                newRequest.setIdUsuario(2); // User 1 trying to create for user 2

                assertThrows(AuthorizationDeniedException.class,
                        () -> solicitacaoService.postNewSolicitacaoDoacao(newRequest));
            }
        }

        @Test
        void shouldThrowException_whenSolicitacaoAlreadyExists() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                mockedAppUtils.when(AppUtils::isAdmin).thenReturn(false);

                when(repository.isSolicitacaoDoacaoValid(1)).thenReturn(true);

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                        () -> solicitacaoService.postNewSolicitacaoDoacao(newRequest));

                assertEquals("Já existe uma solicitação de doação em curso para este usuário.", exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("Tests for getSolicitacaoDoacaoByFilter")
    class GetSolicitacaoDoacaoByFilter {
        @Test
        void shouldReturnList_whenFilterIsProvided() {
            when(repository.getSolicitacaoDoacaoByFilter(1, null, null, null, null, null))
                    .thenReturn(List.of(solicitacao));

            List<SolicitacaoDoacao> result = solicitacaoService.getSolicitacaoDoacaoByFilter(1, null, null, null, null, null);

            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
        }

        @Test
        void shouldThrowException_whenNoFilterIsProvided() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> solicitacaoService.getSolicitacaoDoacaoByFilter(null, null, null, null, null, null));

            assertEquals("Algum parâmetro deve ser informado na requisição.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests for getSolicitacaoDoacaoInfoById")
    class GetSolicitacaoDoacaoInfoById {
        @Test
        void shouldReturnSolicitacao_whenFound() {
            when(repository.getSolicitacaoDoacaoInfoById(1)).thenReturn(Optional.of(solicitacao));
            SolicitacaoDoacao result = solicitacaoService.getSolicitacaoDoacaoInfoById(1);
            assertNotNull(result);
            assertEquals(1, result.getId());
        }

        @Test
        void shouldThrowNotFoundException_whenNotFound() {
            when(repository.getSolicitacaoDoacaoInfoById(1)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> solicitacaoService.getSolicitacaoDoacaoInfoById(1));
        }
    }

    @Nested
    @DisplayName("Tests for patchSolicitacaoDoacaoInfo")
    class PatchSolicitacaoDoacaoInfo {

        private UpdateSolicitacaoDoacaoRequest updateRequest;

        @BeforeEach
        void patchSetup() {
            updateRequest = new UpdateSolicitacaoDoacaoRequest().status("EM_ANDAMENTO");
            when(repository.getSolicitacaoDoacaoInfoById(anyInt())).thenReturn(Optional.of(solicitacao));
        }

        @Test
        void shouldUpdateSuccessfully_forOwner() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                mockedAppUtils.when(AppUtils::isAdmin).thenReturn(false);

                SolicitacaoDoacao result = solicitacaoService.patchSolicitacaoDoacaoInfo(1, updateRequest);

                assertNotNull(result);
                assertEquals(Status.EM_ANDAMENTO, result.getStatus());
                verify(repository).patchSolicitacaoDoacaoInfo(any(SolicitacaoDoacao.class));
            }
        }

        @Test
        void shouldUpdateSuccessfully_forAdmin() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                Login adminLogin = new Login(99, "admin@test.com", "pass", (byte) 1, (byte) 0);
                CustomUserDetails adminDetails = new CustomUserDetails(adminLogin, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(adminDetails);
                mockedAppUtils.when(AppUtils::isAdmin).thenReturn(true);

                solicitacaoService.patchSolicitacaoDoacaoInfo(1, updateRequest);

                verify(repository).patchSolicitacaoDoacaoInfo(argThat(s -> s.getStatus() == Status.EM_ANDAMENTO));
            }
        }

        @Test
        void shouldThrowAccessDenied_whenNotOwnerOrAdmin() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                Login otherLogin = new Login(2, "other@test.com", "pass", (byte) 2, (byte) 0);
                CustomUserDetails otherDetails = new CustomUserDetails(otherLogin, Collections.emptyList());
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(otherDetails);
                mockedAppUtils.when(AppUtils::isAdmin).thenReturn(false);

                assertThrows(AccessDeniedException.class,
                        () -> solicitacaoService.patchSolicitacaoDoacaoInfo(1, updateRequest));
            }
        }

        @Test
        void shouldThrowException_whenStatusIsClosed() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                solicitacao.setStatus(Status.ENCERRADA);

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                        () -> solicitacaoService.patchSolicitacaoDoacaoInfo(1, updateRequest));

                assertEquals("Não é possível atualizar uma solicitação após seu cancelamento ou encerramento.", exception.getMessage());
            }
        }

        @Test
        void shouldThrowException_whenNoFieldsToUpdate() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                updateRequest.setStatus("ABERTA"); // Same as current status

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                        () -> solicitacaoService.patchSolicitacaoDoacaoInfo(1, updateRequest));

                assertEquals("Informe ao menos um campo para atualizar.", exception.getMessage());
            }
        }

        @Test
        void shouldThrowException_forInvalidStatusTransition() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                solicitacao.setStatus(Status.EM_ANDAMENTO);
                updateRequest.setStatus("ABERTA");

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                        () -> solicitacaoService.patchSolicitacaoDoacaoInfo(1, updateRequest));

                assertEquals("Não é possível retornar o status de 'EM_ANDAMENTO' para 'ABERTA'.", exception.getMessage());
            }
        }

        @Test
        void shouldSetEndDate_whenStatusIsEncerrada() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                updateRequest.setStatus("ENCERRADA");

                SolicitacaoDoacao result = solicitacaoService.patchSolicitacaoDoacaoInfo(1, updateRequest);

                assertNotNull(result.getDataEncerramento());
                assertEquals(Status.ENCERRADA, result.getStatus());
            }
        }

        @Test
        void shouldThrowException_forInvalidStatusString() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                updateRequest.setStatus("INVALID_STATUS");

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                        () -> solicitacaoService.patchSolicitacaoDoacaoInfo(1, updateRequest));

                assertTrue(exception.getMessage().contains("Status informado inválido"));
            }
        }
    }
}