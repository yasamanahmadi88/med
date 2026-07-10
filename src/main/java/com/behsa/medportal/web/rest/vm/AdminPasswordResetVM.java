package com.behsa.medportal.web.rest.vm;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * View Model object for storing the user's reset key and new password.
 */
public class AdminPasswordResetVM {

    @NotBlank
    @Size(min = ManagedUserVM.PASSWORD_MIN_LENGTH, max = ManagedUserVM.PASSWORD_MAX_LENGTH)
    @Pattern(regexp = ManagedUserVM.PASSWORD_PATTERN, message = ManagedUserVM.PASSWORD_PATTERN_MESSAGE)
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
