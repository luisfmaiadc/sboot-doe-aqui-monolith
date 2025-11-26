package com.doeaqui.sboot_doe_aqui_monolith.service.Impl;

import com.doeaqui.sboot_doe_aqui_monolith.domain.TipoSanguineo;
import com.doeaqui.sboot_doe_aqui_monolith.repository.TipoSanguineoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class TipoSanguineoServiceImplTest {

    @Autowired
    private TipoSanguineoServiceImpl tipoSanguineoService;

    @MockitoBean
    private TipoSanguineoRepository repository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        List<TipoSanguineo> todosOsTiposSanguineos = List.of(
                new TipoSanguineo((byte) 1, "A", '+'),
                new TipoSanguineo((byte) 2, "A", '-'),
                new TipoSanguineo((byte) 3, "B", '+'),
                new TipoSanguineo((byte) 4, "B", '-'),
                new TipoSanguineo((byte) 5, "AB", '+'),
                new TipoSanguineo((byte) 6, "AB", '-'),
                new TipoSanguineo((byte) 7, "O", '+'),
                new TipoSanguineo((byte) 8, "O", '-')
        );
        when(repository.getTiposSanguineos()).thenReturn(todosOsTiposSanguineos);
    }

    @AfterEach
    void tearDown() {
        Objects.requireNonNull(cacheManager.getCache("tiposSanguineos")).clear();
    }

    @Nested
    @DisplayName("Tests for getTiposSanguineos")
    class GetTiposSanguineos {

        @Test
        @DisplayName("Should return list from repository on first call")
        void getTiposSanguineos_shouldReturnListFromRepository() {
            List<TipoSanguineo> result = tipoSanguineoService.getTiposSanguineos();

            assertNotNull(result);
            assertEquals(8, result.size());
            verify(repository, times(1)).getTiposSanguineos();
        }

        @Test
        @DisplayName("Should use cache on subsequent calls")
        void getTiposSanguineos_shouldUseCache() {
            tipoSanguineoService.getTiposSanguineos();
            tipoSanguineoService.getTiposSanguineos();

            verify(repository, times(1)).getTiposSanguineos();
        }
    }

    @Nested
    @DisplayName("Tests for getTipoSanguineoById")
    class GetTipoSanguineoById {

        @Test
        @DisplayName("Should return correct string for a valid ID")
        void getTipoSanguineoById_shouldReturnCorrectStringForValidId() {
            String result = tipoSanguineoService.getTipoSanguineoById((byte) 1);
            assertEquals("A+", result);

            String result2 = tipoSanguineoService.getTipoSanguineoById((byte) 8);
            assertEquals("O-", result2);
        }

        @Test
        @DisplayName("Should throw exception for an invalid ID")
        void getTipoSanguineoById_shouldThrowExceptionForInvalidId() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> tipoSanguineoService.getTipoSanguineoById((byte) 99));

            assertEquals("Tipo sanguíneo inválido.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests for validateBloodCompatible")
    class ValidateBloodCompatible {

        @Test
        @DisplayName("Should not throw exception for compatible types (O- to AB+)")
        void validateBloodCompatible_shouldNotThrowExceptionForCompatibleTypes() {
            byte idDoador = 8; // O-
            byte idReceptor = 5; // AB+

            assertDoesNotThrow(() -> tipoSanguineoService.validateBloodCompatible(idReceptor, idDoador));
        }

        @Test
        @DisplayName("Should throw exception for incompatible types (A+ to O+)")
        void validateBloodCompatible_shouldThrowExceptionForIncompatibleTypes() {
            byte idDoador = 1; // A+
            byte idReceptor = 7; // O+

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> tipoSanguineoService.validateBloodCompatible(idReceptor, idDoador));

            assertEquals("Tipo sanguíneo incompatível.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests for getTipoSanguineoCompativel")
    class GetTipoSanguineoCompativel {

        @Test
        @DisplayName("Should return correct compatible list for donor A-")
        void getTipoSanguineoCompativel_shouldReturnCorrectListForDonorAminus() {
            byte idDoador = 2; // A-
            List<Byte> expectedReceptors = List.of((byte) 1, (byte) 2, (byte) 5, (byte) 6); // A+, A-, AB+, AB-

            List<Byte> result = tipoSanguineoService.getTipoSanguineoCompativel(idDoador);

            assertNotNull(result);
            assertEquals(expectedReceptors.size(), result.size());
            assertTrue(result.containsAll(expectedReceptors));
        }

        @Test
        @DisplayName("Should return correct compatible list for universal donor O-")
        void getTipoSanguineoCompativel_shouldReturnCorrectListForDonorOminus() {
            byte idDoador = 8; // O-
            List<Byte> expectedReceptors = List.of((byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8);

            List<Byte> result = tipoSanguineoService.getTipoSanguineoCompativel(idDoador);

            assertNotNull(result);
            assertEquals(8, result.size());
            assertTrue(result.containsAll(expectedReceptors));
        }

        @Test
        @DisplayName("Should return only AB+ for donor AB+")
        void getTipoSanguineoCompativel_shouldReturnCorrectListForDonorABplus() {
            byte idDoador = 5; // AB+
            List<Byte> result = tipoSanguineoService.getTipoSanguineoCompativel(idDoador);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.contains((byte) 5));
        }

        @Test
        @DisplayName("Should return an empty list for an invalid donor ID")
        void getTipoSanguineoCompativel_shouldReturnEmptyListForInvalidId() {
            List<Byte> result = tipoSanguineoService.getTipoSanguineoCompativel((byte) 99);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}