package com.doeaqui.sboot_doe_aqui_monolith.service.Impl;

import com.doeaqui.sboot_doe_aqui_monolith.domain.Papel;
import com.doeaqui.sboot_doe_aqui_monolith.repository.PapelRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class PapelServiceImplTest {

    @MockitoBean
    private PapelRepository papelRepository;

    @Autowired
    private PapelServiceImpl papelService;

    @Autowired
    private PapelRepository repository;

    @Autowired
    private CacheManager cacheManager;

    private List<Papel> papeis;

    @BeforeEach
    void setUp() {
        papeis = List.of(
                new Papel((byte) 1, "ADMIN"),
                new Papel((byte) 2, "DOADOR"),
                new Papel((byte) 3, "RECEPTOR")
        );
        when(repository.getPapeisUsuarios()).thenReturn(papeis);
    }

    @AfterEach
    void tearDown() {
        Objects.requireNonNull(cacheManager.getCache("papeisUsuarios")).clear();
    }

    @Nested
    @DisplayName("Tests for getPapeisUsuarios")
    class GetPapeisUsuarios {

        @Test
        @DisplayName("Should return list from repository on first call")
        void getPapeisUsuarios_shouldReturnListFromRepository() {
            List<Papel> result = papelService.getPapeisUsuarios();

            assertNotNull(result);
            assertEquals(3, result.size());
            verify(repository, times(1)).getPapeisUsuarios();
        }

        @Test
        @DisplayName("Should use cache on subsequent calls")
        void getPapeisUsuarios_shouldUseCache() {
            papelService.getPapeisUsuarios();
            papelService.getPapeisUsuarios();

            verify(repository, times(1)).getPapeisUsuarios();
        }
    }

    @Nested
    @DisplayName("Tests for getPapelById")
    class GetPapelById {

        @Test
        @DisplayName("Should return correct Papel for a valid ID")
        void getPapelById_shouldReturnPapelForValidId() {
            Papel result = papelService.getPapelById(2); // DOADOR

            assertNotNull(result);
            assertEquals("DOADOR", result.getNome());
        }

        @Test
        @DisplayName("Should throw exception for an invalid ID")
        void getPapelById_shouldThrowExceptionForInvalidId() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> papelService.getPapelById(99));

            assertEquals("Papel não encontrado.", exception.getMessage());
        }
    }
}