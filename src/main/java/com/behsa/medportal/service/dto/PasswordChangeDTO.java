package com.behsa.medportal.service.dto;

import com.behsa.medportal.web.rest.vm.ManagedUserVM;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * A DTO representing a password change request.
 */
public class PasswordChangeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = ManagedUserVM.PASSWORD_MIN_LENGTH, max = ManagedUserVM.PASSWORD_MAX_LENGTH)
    @Pattern(regexp = ManagedUserVM.PASSWORD_PATTERN, message = ManagedUserVM.PASSWORD_PATTERN_MESSAGE)
    private String newPassword;

    public PasswordChangeDTO() {
        // Empty constructor needed for Jackson.
    }

    public PasswordChangeDTO(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
