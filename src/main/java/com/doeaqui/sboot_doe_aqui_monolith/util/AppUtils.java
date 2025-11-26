package com.doeaqui.sboot_doe_aqui_monolith.util;

import com.doeaqui.sboot_doe_aqui_monolith.config.security.CustomUserDetails;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Objects;

@UtilityClass
public class AppUtils {

    public static void requireAtLeastOneNonNull(List<Object> paramsList) {
        if (paramsList.stream().allMatch(Objects::isNull)) throw new IllegalArgumentException("Algum parâmetro deve ser informado na requisição.");
    }

    public static CustomUserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (CustomUserDetails) authentication.getPrincipal();
    }

    public static boolean isAdmin() {
        CustomUserDetails userDetails = getUserDetails();
        return userDetails.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
    }

    public static void validateCpf(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        try {
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += (cpf.charAt(i) - '0') * (10 - i);
            }
            int firstVerifier = 11 - (sum % 11);
            if (firstVerifier >= 10) {
                firstVerifier = 0;
            }

            if ((cpf.charAt(9) - '0') != firstVerifier) {
                throw new IllegalArgumentException("CPF inválido.");
            }

            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += (cpf.charAt(i) - '0') * (11 - i);
            }
            int secondVerifier = 11 - (sum % 11);
            if (secondVerifier >= 10) {
                secondVerifier = 0;
            }

            if ((cpf.charAt(10) - '0') != secondVerifier) {
                throw new IllegalArgumentException("CPF inválido.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("CPF deve conter apenas números.");
        }
    }
}