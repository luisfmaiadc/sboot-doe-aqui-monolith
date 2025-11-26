package com.doeaqui.sboot_doe_aqui_monolith.util;

import com.doeaqui.sboot_doe_aqui_monolith.config.security.CustomUserDetails;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Login;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class AppUtilsTest {

    @Nested
    @DisplayName("Tests for requireAtLeastOneNonNull")
    class RequireAtLeastOneNonNullTest {

        @Test
        @DisplayName("Should throw exception when all params are null")
        void shouldThrowException_whenAllParamsAreNull() {
            List<Object> params = Arrays.asList(null, null, null);
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> AppUtils.requireAtLeastOneNonNull(params));
            assertEquals("Algum parâmetro deve ser informado na requisição.", exception.getMessage());
        }

        @Test
        @DisplayName("Should not throw exception when at least one param is not null")
        void shouldNotThrowException_whenOneParamIsNotNull() {
            List<Object> params = Arrays.asList(null, "value", null);
            assertDoesNotThrow(() -> AppUtils.requireAtLeastOneNonNull(params));
        }
    }

    @Nested
    @DisplayName("Tests for SecurityContext methods")
    class SecurityContextTest {

        @Test
        @DisplayName("getUserDetails should return correct user details")
        void getUserDetails_shouldReturnCorrectUserDetails() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            try (MockedStatic<SecurityContextHolder> mockedContext = mockStatic(SecurityContextHolder.class)) {
                mockedContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                Login login = new Login(1, "test@test.com", "pass", (byte) 2, (byte) 0);
                CustomUserDetails expectedUserDetails = new CustomUserDetails(login, Collections.emptyList());
                when(authentication.getPrincipal()).thenReturn(expectedUserDetails);

                CustomUserDetails userDetails = AppUtils.getUserDetails();

                assertNotNull(userDetails);
                assertEquals(1, userDetails.getIdUsuario());
            }
        }

        @Test
        @DisplayName("isAdmin should return true for admin user")
        void isAdmin_shouldReturnTrueForAdmin() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            try (MockedStatic<SecurityContextHolder> mockedContext = mockStatic(SecurityContextHolder.class)) {
                mockedContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                Login login = new Login(1, "admin@test.com", "pass", (byte) 1, (byte) 0);
                CustomUserDetails adminDetails = new CustomUserDetails(login, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
                when(authentication.getPrincipal()).thenReturn(adminDetails);

                assertTrue(AppUtils.isAdmin());
            }
        }

        @Test
        @DisplayName("isAdmin should return false for non-admin user")
        void isAdmin_shouldReturnFalseForNonAdmin() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            try (MockedStatic<SecurityContextHolder> mockedContext = mockStatic(SecurityContextHolder.class)) {
                mockedContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                Login login = new Login(2, "user@test.com", "pass", (byte) 2, (byte) 0);
                CustomUserDetails userDetails = new CustomUserDetails(login, List.of(new SimpleGrantedAuthority("ROLE_DOADOR")));
                when(authentication.getPrincipal()).thenReturn(userDetails);

                assertFalse(AppUtils.isAdmin());
            }
        }
    }

    @Nested
    @DisplayName("Tests for validateCpf")
    class ValidateCpfTest {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"1234567890", "123456789012", "11111111111", "abcdefghijk", "123.456.789-01", "99988877765"})
        @DisplayName("Should throw exception for invalid CPFs")
        void shouldThrowException_forInvalidCpfs(String cpf) {
            assertThrows(IllegalArgumentException.class, () -> AppUtils.validateCpf(cpf));
        }

        @ParameterizedTest
        @ValueSource(strings = {"57631801940", "83512748287", "17332593254"})
        @DisplayName("Should not throw exception for valid CPFs")
        void shouldNotThrowException_forValidCpfs(String cpf) {
            assertDoesNotThrow(() -> AppUtils.validateCpf(cpf));
        }
    }
}