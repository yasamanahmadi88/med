package com.behsa.medportal.vaidators;

import com.behsa.medportal.vaidators.dto.PasswordValidationDto;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates passwords according to security best practices.
 */
@Component
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 64;

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    private static final Set<String> COMMON_PASSWORDS = new HashSet<>(Arrays.asList(
        "123456",
        "password",
        "123456789",
        "12345678",
        "12345",
        "qwerty",
        "abc123",
        "111111",
        "123123",
        "admin",
        "letmein",
        "welcome",
        "monkey",
        "login",
        "iloveyou",
        "1234",
        "admin123",
        "password123"
    ));

    public PasswordValidationDto isValid(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordValidationDto.invalidPassword("emptyPassword");
        }

        if (password.length() < MIN_LENGTH) {
            return PasswordValidationDto.invalidPassword("passwordSmallerThanMinLength");
        }

        if (password.length() > MAX_LENGTH) {
            return PasswordValidationDto.invalidPassword("passwordGreaterThanMaxLength");
        }

        if (!UPPERCASE.matcher(password).find()) {
            return PasswordValidationDto.invalidPassword("passwordMustContainAtLeastOneUppercaseLetter");
        }

        if (!LOWERCASE.matcher(password).find()) {
            return PasswordValidationDto.invalidPassword("passwordMustContainAtLeastOneLowercaseLetter");
        }

        if (!DIGIT.matcher(password).find()) {
            return PasswordValidationDto.invalidPassword("passwordMustContainAtLeastOneDigit");
        }

        if (!SPECIAL.matcher(password).find()) {
            return PasswordValidationDto.invalidPassword("passwordMustContainAtLeastOneSpecialCharacter");
        }

        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            return PasswordValidationDto.invalidPassword("passwordIsTooCommonAndInsecure");
        }

        return PasswordValidationDto.validPassword();
    }
}
