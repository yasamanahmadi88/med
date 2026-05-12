package com.behsa.medportal.web.rest.vm;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * View Model object for storing the user's reset key and new password.
 */
public class KeyAndPasswordVM {

    @NotBlank
    @Size(min = 20, max = 20)
    @Pattern(regexp = "^[A-Za-z0-9]+$")
    private String key;

    @NotBlank
    @Size(min = 8, max = 100)
    private String newPassword;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
