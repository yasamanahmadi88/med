package com.behsa.medportal.web.rest;

import com.behsa.medportal.domain.User;
import com.behsa.medportal.repository.UserRepository;
import com.behsa.medportal.security.AuthoritiesConstants;
import com.behsa.medportal.security.SecurityUtils;
import com.behsa.medportal.service.EmailAlreadyUsedException;
import com.behsa.medportal.service.MailService;
import com.behsa.medportal.service.UserService;
import com.behsa.medportal.service.UsernameAlreadyUsedException;
import com.behsa.medportal.service.dto.AdminUserDTO;
import com.behsa.medportal.service.dto.PasswordChangeDTO;
import com.behsa.medportal.service.dto.UserDTO;
import com.behsa.medportal.vaidators.PasswordValidator;
import com.behsa.medportal.vaidators.dto.PasswordValidationDto;
import com.behsa.medportal.web.rest.errors.InvalidPasswordException;
import com.behsa.medportal.web.rest.vm.AdminPasswordResetVM;
import com.behsa.medportal.web.rest.vm.KeyAndPasswordVM;
import com.behsa.medportal.web.rest.vm.ManagedUserVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private static class AccountResourceException extends RuntimeException {

        private AccountResourceException(String message) {
            super(message);
        }
    }

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    private final UserRepository userRepository;

    private final UserService userService;

    private final MailService mailService;

    private final PasswordValidator passwordValidator;

    public AccountResource(
        UserRepository userRepository,
        UserService userService,
        MailService mailService,
        PasswordValidator passwordValidator
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.mailService = mailService;
        this.passwordValidator = passwordValidator;
    }

    /**
     * {@code POST /register} : register the user.
     *
     * Public registration returns a generic accepted response to avoid revealing
     * whether a login or email address already exists.
     *
     * In this deployment, public self-registration should be disabled in
     * SecurityConfiguration.
     *
     * @param managedUserVM the managed user View Model.
     * @return {@code 202 Accepted}.
     */
    @PostMapping("/register")
    public ResponseEntity<Void> registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {
        validatePasswordOrThrow(managedUserVM.getPassword());

        try {
            User user = userService.registerUser(managedUserVM, managedUserVM.getPassword());
            mailService.sendActivationEmail(user);
        } catch (UsernameAlreadyUsedException | EmailAlreadyUsedException ex) {
            log.info("Public registration request ignored because login/email is already in use.");
        } catch (DataIntegrityViolationException ex) {
            log.warn("Public registration request ignored because of unique constraint violation.");
        }

        return ResponseEntity.accepted().build();
    }

    /**
     * {@code GET /activate} : activate the registered user.
     *
     * @param key the activation key.
     */
    @GetMapping("/activate")
    public void activateAccount(@RequestParam(value = "key") String key) {
        Optional<User> user = userService.activateRegistration(key);

        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this activation key");
        }
    }

    /**
     * {@code GET /authenticate} : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request.
     * @return the login if the user is authenticated.
     */
    @GetMapping("/authenticate")
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * {@code GET /account} : get the current user.
     *
     * @return the current user.
     */
    @GetMapping("/account")
    public AdminUserDTO getAccount() {
        AdminUserDTO userDto = userService
            .getUserWithAuthorities()
            .map(AdminUserDTO::new)
            .orElseThrow(() -> new AccountResourceException("User could not be found"));

        SecurityUtils.getCurrentUser().ifPresent(portalUser -> {
            userDto.setPartyId(portalUser.getPartyId());
            userDto.setResourceAuthorities(portalUser.getResourceAuthorities());
        });

        return userDto;
    }

    /**
     * {@code POST /account} : update the current user information.
     *
     * @param userDTO the current user information.
     */
    @PostMapping("/account")
    public void saveAccount(@Valid @RequestBody AdminUserDTO userDTO) {
        String userLogin = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() -> new AccountResourceException("Current user login not found"));

        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());

        if (existingUser.isPresent() && !existingUser.orElseThrow().getLogin().equalsIgnoreCase(userLogin)) {
            throw new EmailAlreadyUsedException();
        }

        Optional<User> user = userRepository.findOneByLogin(userLogin);

        if (!user.isPresent()) {
            throw new AccountResourceException("User could not be found");
        }

        userService.updateUser(
            userDTO.getFirstName(),
            userDTO.getLastName(),
            userDTO.getEmail(),
            userDTO.getLangKey(),
            userDTO.getImageUrl()
        );
    }

    /**
     * {@code POST /account/change-password} : changes the current user's password.
     *
     * @param passwordChangeDto current and new password.
     */
    @PostMapping(path = "/account/change-password")
    public void changePassword(@Valid @RequestBody PasswordChangeDTO passwordChangeDto) {
        validatePasswordOrThrow(passwordChangeDto.getNewPassword());

        userService.changePassword(
            passwordChangeDto.getCurrentPassword(),
            passwordChangeDto.getNewPassword()
        );
    }

    /**
     * {@code POST /password/validate} : validate password policy.
     *
     * This endpoint is useful for frontend password validation.
     *
     * @param request request body containing password.
     * @return password validation result.
     */
    @PostMapping("/password/validate")
    public PasswordValidationDto validatePassword(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        return passwordValidator.isValid(password);
    }

    /**
     * {@code POST /account/reset-password/init} : send a password reset email.
     *
     * This endpoint intentionally returns the same public response whether the
     * email exists or not, to prevent user enumeration.
     *
     * @param mail the mail of the user.
     */
    @PostMapping(path = "/account/reset-password/init")
    public void requestPasswordReset(HttpServletRequest request) throws java.io.IOException {
        String mail = new String(request.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
        // Strip optional JSON quotes when clients send a JSON string body.
        if (mail.length() >= 2 && mail.startsWith("\"") && mail.endsWith("\"")) {
            mail = mail.substring(1, mail.length() - 1);
        }
        Optional<User> user = userService.requestPasswordReset(mail);

        if (user.isPresent()) {
            mailService.sendPasswordResetMail(user.orElseThrow());
        } else {
            log.warn("Password reset requested for non existing mail.");
        }
    }

    /**
     * {@code POST /account/reset-password/finish} : finish resetting the password.
     *
     * Invalid or expired reset keys return a generic bad request response.
     *
     * @param keyAndPassword the generated key and the new password.
     * @return {@code 204 No Content} if reset succeeded, otherwise {@code 400 Bad Request}.
     */
    @PostMapping(path = "/account/reset-password/finish")
    public ResponseEntity<Void> finishPasswordReset(@Valid @RequestBody KeyAndPasswordVM keyAndPassword) {
        validatePasswordOrThrow(keyAndPassword.getNewPassword());

        Optional<UserDTO> user = userService.completePasswordReset(
            keyAndPassword.getNewPassword(),
            keyAndPassword.getKey()
        );

        if (!user.isPresent()) {
            log.info("Password reset finish request ignored because reset key was invalid or expired.");
            return ResponseEntity.badRequest().build();
        }

        log.info("Password reset completed.");
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code POST /admin/users/{login}/reset-password} : reset a user's password by admin.
     *
     * This endpoint is intended for administrative user management only.
     *
     * @param login the login of the target user.
     * @param passwordResetVM the new password request body.
     * @return {@code 204 No Content} if changed, otherwise {@code 400 Bad Request}.
     */
    @Secured(AuthoritiesConstants.ADMIN)
    @PostMapping("/admin/users/{login}/reset-password")
    public ResponseEntity<Void> resetUserPasswordByAdmin(
        @PathVariable String login,
        @Valid @RequestBody AdminPasswordResetVM passwordResetVM
    ) {
        validatePasswordOrThrow(passwordResetVM.getNewPassword());

        boolean changed = userService.resetPasswordByAdmin(
            login,
            passwordResetVM.getNewPassword()
        );

        if (!changed) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.noContent().build();
    }

    private void validatePasswordOrThrow(String password) {
        PasswordValidationDto validation = passwordValidator.isValid(password);

        if (!validation.isValid()) {
            throw new InvalidPasswordException(validation.getValidationException());
        }
    }
}
