package com.behsa.medportal.vaidators.dto;

public class PasswordValidationDto {

    private boolean valid;
    private String validationException;

    public PasswordValidationDto() {
        // Needed for Jackson.
    }

    private PasswordValidationDto(boolean valid, String validationException) {
        this.valid = valid;
        this.validationException = validationException;
    }

    public static PasswordValidationDto validPassword() {
        return new PasswordValidationDto(true, null);
    }

    public static PasswordValidationDto invalidPassword(String validationException) {
        return new PasswordValidationDto(false, validationException);
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getValidationException() {
        return validationException;
    }

    public void setValidationException(String validationException) {
        this.validationException = validationException;
    }

    @Override
    public String toString() {
        return "PasswordValidationDto{" +
            "valid=" + valid +
            ", validationException='" + validationException + '\'' +
            '}';
    }
}
