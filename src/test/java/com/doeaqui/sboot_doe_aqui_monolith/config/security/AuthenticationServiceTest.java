package com.doeaqui.sboot_doe_aqui_monolith.config.security;

import com.doeaqui.sboot_doe_aqui_monolith.domain.Login;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Papel;
import com.doeaqui.sboot_doe_aqui_monolith.repository.LoginRepository;
import com.doeaqui.sboot_doe_aqui_monolith.service.PapelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private PapelService papelService;

    @Mock
    private LoginRepository loginRepository;

    private Login login;
    private final String userEmail = "test@test.com";

    @BeforeEach
    void setUp() {
        login = new Login();
        login.setIdUsuario(1);
        login.setEmail(userEmail);
        login.setSenha("password");
        login.setIdPapel((byte) 2); // DOADOR
    }

    @Nested
    @DisplayName("Tests for loadUserByUsername")
    class LoadUserByUsernameTest {

        @Test
        @DisplayName("Should return UserDetails when user and role are found")
        void shouldReturnUserDetails_whenUserAndRoleAreFound() {
            when(loginRepository.findByEmail(userEmail)).thenReturn(login);
            when(papelService.getPapeisUsuarios()).thenReturn(List.of(new Papel((byte) 2, "DOADOR")));

            UserDetails userDetails = authenticationService.loadUserByUsername(userEmail);

            assertNotNull(userDetails);
            assertEquals(userEmail, userDetails.getUsername());
            assertTrue(userDetails.getAuthorities().stream()
                    .anyMatch(ga -> ga.getAuthority().equals("ROLE_DOADOR")));
        }

        @Test
        @DisplayName("Should throw AuthorizationDeniedException when user is not found")
        void shouldThrowException_whenUserIsNotFound() {
            when(loginRepository.findByEmail(userEmail)).thenReturn(null);

            AuthorizationDeniedException exception = assertThrows(AuthorizationDeniedException.class,
                    () -> authenticationService.loadUserByUsername(userEmail));

            assertEquals("Usuário inexistente.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw AuthorizationDeniedException when role is not found")
        void shouldThrowException_whenRoleIsNotFound() {
            when(loginRepository.findByEmail(userEmail)).thenReturn(login);
            when(papelService.getPapeisUsuarios()).thenReturn(Collections.emptyList()); // No roles available

            AuthorizationDeniedException exception = assertThrows(AuthorizationDeniedException.class,
                    () -> authenticationService.loadUserByUsername(userEmail));

            assertEquals("Role informada é inválida.", exception.getMessage());
        }
    }
}