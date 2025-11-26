package com.doeaqui.sboot_doe_aqui_monolith.service.Impl;

import com.doeaqui.sboot_doe_aqui_monolith.config.exception.ResourceNotFoundException;
import com.doeaqui.sboot_doe_aqui_monolith.config.security.CustomUserDetails;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Doacao;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Login;
import com.doeaqui.sboot_doe_aqui_monolith.domain.SolicitacaoDoacao;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.DoacaoMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.NewDoacaoRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UpdateDoacaoRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UsuarioResponse;
import com.doeaqui.sboot_doe_aqui_monolith.repository.DoacaoRepository;
import com.doeaqui.sboot_doe_aqui_monolith.service.HemocentroService;
import com.doeaqui.sboot_doe_aqui_monolith.service.SolicitacaoDoacaoService;
import com.doeaqui.sboot_doe_aqui_monolith.service.TipoSanguineoService;
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
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoacaoServiceImplTest {

    @InjectMocks
    private DoacaoServiceImpl doacaoService;

    @Mock
    private DoacaoRepository repository;

    @Mock
    private DoacaoMapper mapper;

    @Mock
    private HemocentroService hemocentroService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private SolicitacaoDoacaoService solicitacaoDoacaoService;
    
    @Mock
    private TipoSanguineoService tipoSanguineoService;

    private CustomUserDetails userDetails;
    private UsuarioResponse usuarioResponse;
    private Doacao doacao;
    private NewDoacaoRequest newDoacaoRequest;

    @BeforeEach
    void setUp() {
        Login login = new Login(1, "test@test.com", "pass", (byte) 2, (byte) 0);
        userDetails = new CustomUserDetails(login, List.of());

        usuarioResponse = new UsuarioResponse().id(1).genero("M").idTipoSanguineo(1);

        newDoacaoRequest = new NewDoacaoRequest().idHemocentro(1).volume(450);

        doacao = new Doacao();
        doacao.setId(1);
        doacao.setIdUsuario(1);
        doacao.setIdHemocentro(1);
        doacao.setVolume(450);
        doacao.setDataDoacao(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Tests for postNewDoacao")
    class PostNewDoacao {
        @Test
        void postNewDoacao_shouldCreateSuccessfully() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                when(usuarioService.getUserInfoById(1)).thenReturn(usuarioResponse);
                when(mapper.toDoacao(newDoacaoRequest)).thenReturn(doacao);
                when(repository.getUltimaDoacao(1)).thenReturn(Optional.empty());
                when(repository.postNewDoacao(doacao)).thenReturn(1);
                when(repository.getDoacaoInfoById(1)).thenReturn(Optional.of(doacao));

                Doacao result = doacaoService.postNewDoacao(newDoacaoRequest);

                assertNotNull(result);
                assertEquals(1, result.getId());
                verify(hemocentroService).getHemocentroInfoById(1);
            }
        }

        @Test
        void postNewDoacao_shouldThrowException_whenVolumeIsInvalid() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                when(usuarioService.getUserInfoById(1)).thenReturn(usuarioResponse);
                doacao.setVolume(600);
                when(mapper.toDoacao(newDoacaoRequest)).thenReturn(doacao);

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                        () -> doacaoService.postNewDoacao(newDoacaoRequest));

                assertEquals("Volume de sangue da doação é inválido.", exception.getMessage());
            }
        }

        @Test
        void postNewDoacao_shouldThrowException_whenDonationIntervalNotMet() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                when(usuarioService.getUserInfoById(1)).thenReturn(usuarioResponse);
                when(mapper.toDoacao(newDoacaoRequest)).thenReturn(doacao);
                Doacao lastDonation = new Doacao();
                lastDonation.setDataDoacao(LocalDateTime.now().minusMonths(1));
                when(repository.getUltimaDoacao(1)).thenReturn(Optional.of(lastDonation));

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                        () -> doacaoService.postNewDoacao(newDoacaoRequest));

                assertTrue(exception.getMessage().contains("Intervalo mínimo entre doações não atingido."));
            }
        }

        @Test
        void postNewDoacao_shouldValidateBloodTypeForSolicitation() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                when(usuarioService.getUserInfoById(1)).thenReturn(usuarioResponse);
                doacao.setIdSolicitacaoDoacao(10);
                when(mapper.toDoacao(newDoacaoRequest)).thenReturn(doacao);
                when(solicitacaoDoacaoService.getSolicitacaoDoacaoInfoById(10)).thenReturn(new SolicitacaoDoacao());

                when(repository.postNewDoacao(any(Doacao.class))).thenReturn(1);
                when(repository.getDoacaoInfoById(1)).thenReturn(Optional.of(doacao));

                doacaoService.postNewDoacao(newDoacaoRequest);

                verify(tipoSanguineoService).validateBloodCompatible(any(), anyByte());
            }
        }
    }

    @Nested
    @DisplayName("Tests for getDoacaoInfoById")
    class GetDoacaoInfoById {
        @Test
        void getDoacaoInfoById_shouldReturnDonation_forOwner() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                mockedAppUtils.when(AppUtils::isAdmin).thenReturn(false);
                when(repository.getDoacaoInfoById(1)).thenReturn(Optional.of(doacao));

                Doacao result = doacaoService.getDoacaoInfoById(1);

                assertNotNull(result);
                assertEquals(1, result.getId());
            }
        }

        @Test
        void getDoacaoInfoById_shouldReturnDonation_forAdmin() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                Login adminLogin = new Login(99, "admin@test.com", "pass", (byte) 1, (byte) 0);
                CustomUserDetails adminDetails = new CustomUserDetails(adminLogin, List.of());
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(adminDetails);
                mockedAppUtils.when(AppUtils::isAdmin).thenReturn(true);
                when(repository.getDoacaoInfoById(1)).thenReturn(Optional.of(doacao));

                Doacao result = doacaoService.getDoacaoInfoById(1);

                assertNotNull(result);
            }
        }

        @Test
        void getDoacaoInfoById_shouldThrowAuthorizationDenied() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                Login otherLogin = new Login(2, "other@test.com", "pass", (byte) 2, (byte) 0);
                CustomUserDetails otherDetails = new CustomUserDetails(otherLogin, List.of());
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(otherDetails);
                mockedAppUtils.when(AppUtils::isAdmin).thenReturn(false);
                when(repository.getDoacaoInfoById(1)).thenReturn(Optional.of(doacao));

                assertThrows(AuthorizationDeniedException.class, () -> doacaoService.getDoacaoInfoById(1));
            }
        }

        @Test
        void getDoacaoInfoById_shouldThrowNotFound() {
            when(repository.getDoacaoInfoById(1)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> doacaoService.getDoacaoInfoById(1));
        }
    }

    @Nested
    @DisplayName("Tests for getDoacaoByFilter")
    class GetDoacaoByFilter {
        @Test
        void getDoacaoByFilter_shouldReturnListForUser() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                mockedAppUtils.when(AppUtils::isAdmin).thenReturn(false);
                when(repository.getDoacaoByFilter(1, null, null, 1)).thenReturn(List.of(doacao));

                List<Doacao> result = doacaoService.getDoacaoByFilter(null, 1, null, null);

                assertFalse(result.isEmpty());
                verify(repository).getDoacaoByFilter(1, null, null, 1);
            }
        }

        @Test
        void getDoacaoByFilter_shouldThrowAuthorizationDenied_whenUserTriesToFilterOtherUser() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                mockedAppUtils.when(AppUtils::isAdmin).thenReturn(false);

                assertThrows(AuthorizationDeniedException.class,
                        () -> doacaoService.getDoacaoByFilter(2, null, null, null));
            }
        }

        @Test
        void getDoacaoByFilter_shouldAllowAdminToFilterOtherUser() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                mockedAppUtils.when(AppUtils::isAdmin).thenReturn(true);
                when(repository.getDoacaoByFilter(null, LocalDate.now(), 450, 2)).thenReturn(List.of(doacao));

                List<Doacao> result = doacaoService.getDoacaoByFilter(2, null, LocalDate.now(), 450);

                assertFalse(result.isEmpty());
                verify(repository).getDoacaoByFilter(null, LocalDate.now(), 450, 2);
            }
        }
    }

    @Nested
    @DisplayName("Tests for patchDoacaoInfo")
    class PatchDoacaoInfo {
        @Test
        void patchDoacaoInfo_shouldUpdateSuccessfully() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                mockedAppUtils.when(AppUtils::isAdmin).thenReturn(false);
                when(repository.getDoacaoInfoById(1)).thenReturn(Optional.of(doacao));
                UpdateDoacaoRequest request = new UpdateDoacaoRequest().observacoes("New observation");

                doacaoService.patchDoacaoInfo(1, request);

                verify(repository).patchDoacaoInfo(argThat(d -> d.getObservacoes().equals("New observation")));
            }
        }

        @Test
        void patchDoacaoInfo_shouldThrowAuthorizationDenied() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                Login otherLogin = new Login(2, "other@test.com", "pass", (byte) 2, (byte) 0);
                CustomUserDetails otherDetails = new CustomUserDetails(otherLogin, List.of());
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(otherDetails);
                mockedAppUtils.when(AppUtils::isAdmin).thenReturn(false);
                when(repository.getDoacaoInfoById(1)).thenReturn(Optional.of(doacao));
                UpdateDoacaoRequest request = new UpdateDoacaoRequest().observacoes("New observation");

                assertThrows(AuthorizationDeniedException.class,
                        () -> doacaoService.patchDoacaoInfo(1, request));
            }
        }

        @Test
        void patchDoacaoInfo_shouldThrowException_whenNoFieldsProvided() {
            UpdateDoacaoRequest request = new UpdateDoacaoRequest();
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> doacaoService.patchDoacaoInfo(1, request));

            assertEquals("Algum parâmetro deve ser informado na requisição.", exception.getMessage());
        }
    }
}