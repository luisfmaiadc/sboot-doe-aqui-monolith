package com.doeaqui.sboot_doe_aqui_monolith.service.Impl;

import com.doeaqui.sboot_doe_aqui_monolith.config.exception.ResourceNotFoundException;
import com.doeaqui.sboot_doe_aqui_monolith.config.security.CustomUserDetails;
import com.doeaqui.sboot_doe_aqui_monolith.domain.EnderecoHemocentro;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Hemocentro;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Login;
import com.doeaqui.sboot_doe_aqui_monolith.mapper.HemocentroMapper;
import com.doeaqui.sboot_doe_aqui_monolith.model.HemocentroPorLocalizacaoResponse;
import com.doeaqui.sboot_doe_aqui_monolith.model.NewHemocentroRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UpdateHemocentroRequest;
import com.doeaqui.sboot_doe_aqui_monolith.model.UsuarioResponse;
import com.doeaqui.sboot_doe_aqui_monolith.repository.EnderecoHemocentroRepository;
import com.doeaqui.sboot_doe_aqui_monolith.repository.HemocentroRepository;
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
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HemocentroServiceImplTest {

    @InjectMocks
    private HemocentroServiceImpl hemocentroService;

    @Mock
    private HemocentroRepository repository;

    @Mock
    private EnderecoHemocentroRepository enderecoHemocentroRepository;

    @Mock
    private HemocentroMapper mapper;

    @Mock
    private UsuarioService usuarioService;
    
    @Mock
    private TipoSanguineoService tipoSanguineoService;

    private Hemocentro hemocentro;
    private EnderecoHemocentro endereco;
    private NewHemocentroRequest newHemocentroRequest;
    private UpdateHemocentroRequest updateHemocentroRequest;

    @BeforeEach
    void setUp() {
        endereco = new EnderecoHemocentro();
        endereco.setId(1);
        endereco.setRua("Rua Teste");

        hemocentro = new Hemocentro();
        hemocentro.setId(1);
        hemocentro.setNome("Hemo Teste");
        hemocentro.setAtivo(true);
        hemocentro.setEndereco(endereco);

        newHemocentroRequest = new NewHemocentroRequest();
        updateHemocentroRequest = new UpdateHemocentroRequest();
    }

    @Nested
    @DisplayName("Tests for postNewHemocentro")
    class PostNewHemocentro {
        @Test
        void postNewHemocentro_shouldCreateSuccessfully() {
            when(mapper.toHemocentro(newHemocentroRequest)).thenReturn(hemocentro);
            when(repository.postNewHemocentro(hemocentro)).thenReturn(1);
            when(repository.getHemocentroInfoById(1)).thenReturn(Optional.of(hemocentro));
            when(enderecoHemocentroRepository.findById(1)).thenReturn(Optional.of(endereco));

            Hemocentro result = hemocentroService.postNewHemocentro(newHemocentroRequest);

            assertNotNull(result);
            assertEquals(1, result.getId());
            verify(repository).postNewHemocentro(any(Hemocentro.class));
            verify(enderecoHemocentroRepository).save(any(EnderecoHemocentro.class));
        }
    }

    @Nested
    @DisplayName("Tests for getHemocentroInfoById")
    class GetHemocentroInfoById {
        @Test
        void getHemocentroInfoById_shouldReturnHemocentro() {
            when(repository.getHemocentroInfoById(1)).thenReturn(Optional.of(hemocentro));
            when(enderecoHemocentroRepository.findById(1)).thenReturn(Optional.of(endereco));

            Hemocentro result = hemocentroService.getHemocentroInfoById(1);

            assertNotNull(result);
            assertEquals(1, result.getId());
            assertNotNull(result.getEndereco());
        }

        @Test
        void getHemocentroInfoById_shouldThrowNotFoundException_whenHemocentroMissing() {
            when(repository.getHemocentroInfoById(1)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> hemocentroService.getHemocentroInfoById(1));

            assertEquals("Hemocentro não encontrado.", exception.getMessage());
        }

        @Test
        void getHemocentroInfoById_shouldThrowNotFoundException_whenEnderecoMissing() {
            when(repository.getHemocentroInfoById(1)).thenReturn(Optional.of(hemocentro));
            when(enderecoHemocentroRepository.findById(1)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> hemocentroService.getHemocentroInfoById(1));

            assertEquals("Endereço do hemocentro não encontrado.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests for getHemocentroByFilter")
    class GetHemocentroByFilter {
        @Test
        void getHemocentroByFilter_shouldReturnList() {
            when(repository.getHemocentroByFilter("%Hemo%", null, null)).thenReturn(List.of(hemocentro));
            when(enderecoHemocentroRepository.findById(1)).thenReturn(Optional.of(endereco));

            List<Hemocentro> result = hemocentroService.getHemocentroByFilter("Hemo", null, null);

            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            assertNotNull(result.getFirst().getEndereco());
        }

        @Test
        void getHemocentroByFilter_shouldReturnEmptyList() {
            when(repository.getHemocentroByFilter("%Hemo%", null, null)).thenReturn(Collections.emptyList());

            List<Hemocentro> result = hemocentroService.getHemocentroByFilter("Hemo", null, null);

            assertTrue(result.isEmpty());
        }

        @Test
        void getHemocentroByFilter_shouldThrowException_whenNoFilterProvided() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> hemocentroService.getHemocentroByFilter(null, null, null));

            assertEquals("Algum parâmetro deve ser informado na requisição.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests for patchHemocentroInfo")
    class PatchHemocentroInfo {

        @Test
        void patchHemocentroInfo_shouldUpdateSuccessfully() {
            when(repository.getHemocentroInfoById(1)).thenReturn(Optional.of(hemocentro));
            when(enderecoHemocentroRepository.findById(1)).thenReturn(Optional.of(endereco));
            updateHemocentroRequest.setEmail("new@email.com");

            hemocentroService.patchHemocentroInfo(1, updateHemocentroRequest);

            verify(repository).patchHemocentroInfo(any(Hemocentro.class));
        }

        @Test
        void patchHemocentroInfo_shouldThrowException_whenNoChanges() {
            when(repository.getHemocentroInfoById(1)).thenReturn(Optional.of(hemocentro));
            when(enderecoHemocentroRepository.findById(1)).thenReturn(Optional.of(endereco));
            hemocentro.setEmail("current@email.com");
            updateHemocentroRequest.setEmail("current@email.com");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> hemocentroService.patchHemocentroInfo(1, updateHemocentroRequest));

            assertEquals("Informe ao menos um campo para atualizar.", exception.getMessage());
        }

        @Test
        void patchHemocentroInfo_shouldThrowException_whenNoFieldsProvided() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> hemocentroService.patchHemocentroInfo(1, new UpdateHemocentroRequest()));

            assertEquals("Algum parâmetro deve ser informado na requisição.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests for deleteHemocentro")
    class DeleteHemocentro {
        @Test
        void deleteHemocentro_shouldDeactivateSuccessfully() {
            when(repository.getHemocentroInfoById(1)).thenReturn(Optional.of(hemocentro));
            when(enderecoHemocentroRepository.findById(1)).thenReturn(Optional.of(endereco));

            hemocentroService.deleteHemocentro(1);

            verify(repository).deleteHemocentro(1);
        }

        @Test
        void deleteHemocentro_shouldThrowException_whenAlreadyInactive() {
            hemocentro.setAtivo(false);
            when(repository.getHemocentroInfoById(1)).thenReturn(Optional.of(hemocentro));
            when(enderecoHemocentroRepository.findById(1)).thenReturn(Optional.of(endereco));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> hemocentroService.deleteHemocentro(1));

            assertEquals("Hemocentro já inativado.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests for getHemocentroByLocation")
    class GetHemocentroByLocation {
        private UsuarioResponse currentUser;
        private CustomUserDetails userDetails;

        @BeforeEach
        void locationSetup() {
            currentUser = new UsuarioResponse().id(1).idTipoSanguineo(1);
            Login login = new Login(1, "test@test.com", "pass", (byte) 2, (byte) 0);
            userDetails = new CustomUserDetails(login, List.of());
        }

        @Test
        void getHemocentroByLocation_shouldReturnSortedList() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                when(usuarioService.getUserInfoById(1)).thenReturn(currentUser);

                List<EnderecoHemocentro> nearbyEnderecos = List.of(endereco);
                when(enderecoHemocentroRepository.findByGeoLocationNear(any(Point.class), any(Distance.class)))
                        .thenReturn(nearbyEnderecos);

                List<Integer> nearbyIds = List.of(1);
                when(repository.getHemocentrosInfoByIds(nearbyIds)).thenReturn(List.of(hemocentro));

                List<Byte> compatibleBloodTypes = List.of((byte) 1, (byte) 5);
                when(tipoSanguineoService.getTipoSanguineoCompativel(anyByte())).thenReturn(compatibleBloodTypes);

                Set<Integer> priorityIds = Set.of(1);
                when(repository.getHemocentroIfHasSolicitacaoDoacao(nearbyIds, 1, compatibleBloodTypes))
                        .thenReturn(priorityIds);

                HemocentroPorLocalizacaoResponse responseDto = new HemocentroPorLocalizacaoResponse();
                responseDto.setId(1);
                when(mapper.toHemocentroPorLocalizacaoResponse(hemocentro)).thenReturn(responseDto);

                List<HemocentroPorLocalizacaoResponse> result = hemocentroService.getHemocentroByLocation(0.0, 0.0, 10);

                assertFalse(result.isEmpty());
                assertEquals(1, result.size());
                assertEquals(Boolean.TRUE, result.getFirst().getPrioridade());
            }
        }

        @Test
        void getHemocentroByLocation_shouldReturnEmptyList_whenNoHemocentrosNearby() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                when(enderecoHemocentroRepository.findByGeoLocationNear(any(Point.class), any(Distance.class)))
                        .thenReturn(Collections.emptyList());

                List<HemocentroPorLocalizacaoResponse> result = hemocentroService.getHemocentroByLocation(0.0, 0.0, 10);

                assertTrue(result.isEmpty());
                verify(repository, never()).getHemocentrosInfoByIds(any());
            }
        }

        @Test
        void getHemocentroByLocation_shouldSortNonPriorityLast() {
            try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
                mockedAppUtils.when(AppUtils::getUserDetails).thenReturn(userDetails);
                when(usuarioService.getUserInfoById(1)).thenReturn(currentUser);

                Hemocentro hemo2 = new Hemocentro();
                hemo2.setId(2);
                EnderecoHemocentro end2 = new EnderecoHemocentro();
                end2.setId(2);

                List<EnderecoHemocentro> nearbyEnderecos = List.of(endereco, end2);
                when(enderecoHemocentroRepository.findByGeoLocationNear(any(), any())).thenReturn(nearbyEnderecos);

                List<Integer> nearbyIds = List.of(1, 2);
                when(repository.getHemocentrosInfoByIds(nearbyIds)).thenReturn(List.of(hemocentro, hemo2));

                when(tipoSanguineoService.getTipoSanguineoCompativel(anyByte())).thenReturn(List.of());
                when(repository.getHemocentroIfHasSolicitacaoDoacao(any(), any(), any())).thenReturn(Set.of(2)); // Hemo2 is priority

                HemocentroPorLocalizacaoResponse resp1 = new HemocentroPorLocalizacaoResponse().id(1);
                HemocentroPorLocalizacaoResponse resp2 = new HemocentroPorLocalizacaoResponse().id(2);
                when(mapper.toHemocentroPorLocalizacaoResponse(hemocentro)).thenReturn(resp1);
                when(mapper.toHemocentroPorLocalizacaoResponse(hemo2)).thenReturn(resp2);

                List<HemocentroPorLocalizacaoResponse> result = hemocentroService.getHemocentroByLocation(0.0, 0.0, 10);

                assertFalse(result.isEmpty());
                assertEquals(2, result.size());
                assertEquals(2, result.get(0).getId());
                assertEquals(Boolean.TRUE, result.get(0).getPrioridade());
                assertEquals(1, result.get(1).getId());
                assertNotEquals(Boolean.TRUE, result.get(1).getPrioridade());
            }
        }
    }
}