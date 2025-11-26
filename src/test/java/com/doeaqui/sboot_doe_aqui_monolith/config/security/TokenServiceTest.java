package com.doeaqui.sboot_doe_aqui_monolith.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.doeaqui.sboot_doe_aqui_monolith.domain.Login;
import com.doeaqui.sboot_doe_aqui_monolith.model.AuthenticationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    private final String secret = "test-secret";
    private final String userEmail = "test@user.com";
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "secret", secret);
        Login login = new Login(1, userEmail, "pass", (byte) 2, (byte) 0);
        userDetails = new CustomUserDetails(login, Collections.emptyList());
    }

    @Nested
    @DisplayName("Tests for getToken")
    class GetTokenTest {

        @Test
        @DisplayName("Should generate a valid JWT token")
        void shouldGenerateValidJwtToken() {
            AuthenticationResponse response = tokenService.getToken(userDetails);

            assertNotNull(response);
            assertNotNull(response.getToken());
            assertFalse(response.getToken().isEmpty());

            String subject = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("API DoeAqui")
                    .build()
                    .verify(response.getToken())
                    .getSubject();

            assertEquals(userEmail, subject);
        }

        @Test
        @DisplayName("Should throw RuntimeException on token creation error")
        void shouldThrowRuntimeExceptionOnCreationError() {
            ReflectionTestUtils.setField(tokenService, "secret", null);

            assertThrows(RuntimeException.class, () -> tokenService.getToken(userDetails));
        }
    }

    @Nested
    @DisplayName("Tests for getSubject")
    class GetSubjectTest {

        @Test
        @DisplayName("Should return subject from a valid token")
        void shouldReturnSubjectFromValidToken() {
            String token = tokenService.getToken(userDetails).getToken();
            String subject = tokenService.getSubject(token);

            assertEquals(userEmail, subject);
        }

        @Test
        @DisplayName("Should throw JWTVerificationException for an invalid token")
        void shouldThrowExceptionForInvalidToken() {
            String invalidToken = "this.is.an.invalid.token";

            assertThrows(JWTVerificationException.class, () -> tokenService.getSubject(invalidToken));
        }

        @Test
        @DisplayName("Should throw TokenExpiredException for an expired token")
        void shouldThrowExceptionForExpiredToken() {
            String expiredToken = JWT.create()
                    .withIssuer("API DoeAqui")
                    .withSubject(userEmail)
                    .withExpiresAt(Instant.now().minusSeconds(60))
                    .sign(Algorithm.HMAC256(secret));

            assertThrows(TokenExpiredException.class, () -> tokenService.getSubject(expiredToken));
        }
    }
}