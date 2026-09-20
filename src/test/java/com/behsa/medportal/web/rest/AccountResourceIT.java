package com.behsa.medportal.web.rest;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.config.Constants;
import com.behsa.medportal.domain.Authority;
import com.behsa.medportal.domain.User;
import com.behsa.medportal.repository.AuthorityRepository;
import com.behsa.medportal.repository.UserRepository;
import com.behsa.medportal.security.AuthoritiesConstants;
import com.behsa.medportal.service.dto.AdminUserDTO;
import com.behsa.medportal.service.dto.PasswordChangeDTO;
import com.behsa.medportal.web.rest.vm.KeyAndPasswordVM;
import com.behsa.medportal.web.rest.vm.ManagedUserVM;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static com.behsa.medportal.web.rest.AccountResourceIT.TEST_USER_LOGIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link AccountResource} REST controller.
 */
@AutoConfigureMockMvc
@WithMockUser(value = TEST_USER_LOGIN)
@IntegrationTest
class AccountResourceIT {

    static final String TEST_USER_LOGIN = "test";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc restAccountMockMvc;

    @Test
    @WithUnauthenticatedMockUser
    void testNonAuthenticatedUser() throws Exception {
        restAccountMockMvc
            .perform(get("/api/authenticate").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
    }

    @Test
    void testAuthenticatedUser() throws Exception {
        restAccountMockMvc
            .perform(
                get("/api/authenticate")
                    .with(request -> {
                        request.setRemoteUser(TEST_USER_LOGIN);
                        return request;
                    })
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().string(TEST_USER_LOGIN));
    }

    @Test
    @Transactional
    @WithMockUser("existing-account")
    void testGetExistingAccount() throws Exception {
        String login = "existing-account";

        userRepository.findOneByLogin(login).ifPresent(userRepository::delete);
        userRepository.flush();

        Authority adminAuthority = getAuthority(AuthoritiesConstants.ADMIN);

        User user = new User();
        user.setLogin(login);
        user.setFirstName("john");
        user.setLastName("doe");
        user.setEmail("john.doe@jhipster.com");
        user.setImageUrl("http://placehold.it/50x50");
        user.setLangKey("en");
        user.setPassword(RandomStringUtils.randomAlphanumeric(60));
        user.setActivated(true);
        user.setAuthorities(new HashSet<>(Set.of(adminAuthority)));

        userRepository.saveAndFlush(user);

        assertThat(userRepository.findOneByLogin(login)).isPresent();
        assertThat(userRepository.findOneWithAuthoritiesByLogin(login)).isPresent();

        restAccountMockMvc
            .perform(get("/api/account").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.login").value(login))
            .andExpect(jsonPath("$.firstName").value("john"))
            .andExpect(jsonPath("$.lastName").value("doe"))
            .andExpect(jsonPath("$.email").value("john.doe@jhipster.com"))
            .andExpect(jsonPath("$.imageUrl").value("http://placehold.it/50x50"))
            .andExpect(jsonPath("$.langKey").value("en"))
            .andExpect(jsonPath("$.authorities").isArray())
            .andExpect(jsonPath("$.authorities[0]").value(AuthoritiesConstants.ADMIN));
    }

    @Test
    @Transactional
    @WithMockUser("unknown-account")
    void testGetUnknownAccount() throws Exception {
        userRepository.findOneByLogin("unknown-account").ifPresent(userRepository::delete);
        userRepository.flush();

        restAccountMockMvc
            .perform(get("/api/account").accept(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @Transactional
    @WithUnauthenticatedMockUser
    void testPublicRegisterIsDisabled() throws Exception {
        // Self-registration is denyAll in SecurityConfiguration for this deployment.
        ManagedUserVM validUser = createManagedUser(
            "test-register-valid",
            "test-register-valid@example.com",
            "Password1!"
        );

        assertThat(userRepository.findOneByLogin("test-register-valid")).isEmpty();

        restAccountMockMvc
            .perform(
                post("/api/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(validUser))
            )
            .andExpect(status().isUnauthorized());

        assertThat(userRepository.findOneByLogin("test-register-valid")).isEmpty();
    }

    @Test
    @Transactional
    @WithUnauthenticatedMockUser
    void testPublicRegisterRemainsDisabledForInvalidPayload() throws Exception {
        ManagedUserVM invalidUser = createManagedUser("funky-log(n", "funky@example.com", "Password1!");

        restAccountMockMvc
            .perform(
                post("/api/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(invalidUser))
            )
            .andExpect(status().isUnauthorized());

        assertThat(userRepository.findOneByEmailIgnoreCase("funky@example.com")).isEmpty();
    }

    @Test
    @Transactional
    @WithUnauthenticatedMockUser
    void testPublicRegisterDoesNotCreateUsersOrRevealExistence() throws Exception {
        User existingUser = createActivatedUser("existing-login", "existing-email@example.com");
        userRepository.saveAndFlush(existingUser);

        ManagedUserVM duplicateLoginUser = createManagedUser(
            "existing-login",
            "another-email@example.com",
            "Password1!"
        );
        ManagedUserVM duplicateEmailUser = createManagedUser(
            "new-login-for-existing-email",
            "existing-email@example.com",
            "Password1!"
        );

        restAccountMockMvc
            .perform(
                post("/api/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(duplicateLoginUser))
            )
            .andExpect(status().isUnauthorized());

        restAccountMockMvc
            .perform(
                post("/api/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(duplicateEmailUser))
            )
            .andExpect(status().isUnauthorized());

        assertThat(userRepository.findOneByLogin("new-login-for-existing-email")).isEmpty();
        assertThat(userRepository.findOneByEmailIgnoreCase("another-email@example.com")).isEmpty();
    }

    @Test
    @Transactional
    @WithUnauthenticatedMockUser
    void testActivateAccount() throws Exception {
        final String activationKey = "some activation key";

        User user = new User();
        user.setLogin("activate-account");
        user.setEmail("activate-account@example.com");
        user.setPassword(RandomStringUtils.randomAlphanumeric(60));
        user.setActivated(false);
        user.setActivationKey(activationKey);

        userRepository.saveAndFlush(user);

        restAccountMockMvc
            .perform(get("/api/activate?key={activationKey}", activationKey))
            .andExpect(status().isOk());

        User activatedUser = userRepository.findOneByLogin(user.getLogin()).orElse(null);
        assertThat(activatedUser).isNotNull();
        assertThat(activatedUser.isActivated()).isTrue();
    }

    @Test
    @Transactional
    @WithUnauthenticatedMockUser
    void testActivateAccountWithWrongKey() throws Exception {
        restAccountMockMvc
            .perform(get("/api/activate?key=wrongActivationKey"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @Transactional
    @WithMockUser("save-account")
    void testSaveAccount() throws Exception {
        User user = createActivatedUser("save-account", "save-account@example.com");
        userRepository.saveAndFlush(user);

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("save-account@example.com");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Set.of(AuthoritiesConstants.ADMIN));

        restAccountMockMvc
            .perform(
                post("/api/account")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(userDTO))
            )
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneWithAuthoritiesByLogin(user.getLogin()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getFirstName()).isEqualTo(userDTO.getFirstName());
        assertThat(updatedUser.getLastName()).isEqualTo(userDTO.getLastName());
        assertThat(updatedUser.getEmail()).isEqualTo(userDTO.getEmail());
        assertThat(updatedUser.getLangKey()).isEqualTo(userDTO.getLangKey());
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(updatedUser.getImageUrl()).isEqualTo(userDTO.getImageUrl());
        assertThat(updatedUser.isActivated()).isTrue();
        assertThat(updatedUser.getAuthorities()).isEmpty();
    }

    @Test
    @Transactional
    @WithMockUser("save-invalid-email")
    void testSaveInvalidEmail() throws Exception {
        User user = createActivatedUser("save-invalid-email", "save-invalid-email@example.com");
        userRepository.saveAndFlush(user);

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("invalid email");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Set.of(AuthoritiesConstants.ADMIN));

        restAccountMockMvc
            .perform(
                post("/api/account")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(userDTO))
            )
            .andExpect(status().isBadRequest());

        assertThat(userRepository.findOneByEmailIgnoreCase("invalid email")).isNotPresent();
    }

    @Test
    @Transactional
    @WithMockUser("save-existing-email")
    void testSaveExistingEmail() throws Exception {
        User user = createActivatedUser("save-existing-email", "save-existing-email@example.com");
        userRepository.saveAndFlush(user);

        User anotherUser = createActivatedUser("save-existing-email2", "save-existing-email2@example.com");
        userRepository.saveAndFlush(anotherUser);

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("save-existing-email2@example.com");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Set.of(AuthoritiesConstants.ADMIN));

        restAccountMockMvc
            .perform(
                post("/api/account")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(userDTO))
            )
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin("save-existing-email").orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo("save-existing-email@example.com");
    }

    @Test
    @Transactional
    @WithMockUser("save-existing-email-and-login")
    void testSaveExistingEmailAndLogin() throws Exception {
        User user = createActivatedUser("save-existing-email-and-login", "save-existing-email-and-login@example.com");
        userRepository.saveAndFlush(user);

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("save-existing-email-and-login@example.com");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Set.of(AuthoritiesConstants.ADMIN));

        restAccountMockMvc
            .perform(
                post("/api/account")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(userDTO))
            )
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneByLogin("save-existing-email-and-login").orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo("save-existing-email-and-login@example.com");
    }

    @Test
    @Transactional
    @WithMockUser("change-password-wrong-existing-password")
    void testChangePasswordWrongExistingPassword() throws Exception {
        User user = createActivatedUser(
            "change-password-wrong-existing-password",
            "change-password-wrong-existing-password@example.com"
        );
        String currentPassword = RandomStringUtils.randomAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        userRepository.saveAndFlush(user);

        restAccountMockMvc
            .perform(
                post("/api/account/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO("1" + currentPassword, "Password1!")))
            )
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin("change-password-wrong-existing-password").orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(passwordEncoder.matches("Password1!", updatedUser.getPassword())).isFalse();
        assertThat(passwordEncoder.matches(currentPassword, updatedUser.getPassword())).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser("change-password")
    void testChangePassword() throws Exception {
        User user = createActivatedUser("change-password", "change-password@example.com");
        String currentPassword = RandomStringUtils.randomAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        userRepository.saveAndFlush(user);

        restAccountMockMvc
            .perform(
                post("/api/account/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, "Password1!")))
            )
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneByLogin("change-password").orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(passwordEncoder.matches("Password1!", updatedUser.getPassword())).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser("change-password-too-small")
    void testChangePasswordTooSmall() throws Exception {
        User user = createActivatedUser("change-password-too-small", "change-password-too-small@example.com");
        String currentPassword = RandomStringUtils.randomAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        userRepository.saveAndFlush(user);

        String newPassword = RandomStringUtils.random(ManagedUserVM.PASSWORD_MIN_LENGTH - 1);

        restAccountMockMvc
            .perform(
                post("/api/account/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, newPassword)))
            )
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin("change-password-too-small").orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
    }

    @Test
    @Transactional
    @WithMockUser("change-password-too-long")
    void testChangePasswordTooLong() throws Exception {
        User user = createActivatedUser("change-password-too-long", "change-password-too-long@example.com");
        String currentPassword = RandomStringUtils.randomAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        userRepository.saveAndFlush(user);

        String newPassword = RandomStringUtils.random(ManagedUserVM.PASSWORD_MAX_LENGTH + 1);

        restAccountMockMvc
            .perform(
                post("/api/account/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, newPassword)))
            )
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin("change-password-too-long").orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
    }

    @Test
    @Transactional
    @WithMockUser("change-password-empty")
    void testChangePasswordEmpty() throws Exception {
        User user = createActivatedUser("change-password-empty", "change-password-empty@example.com");
        String currentPassword = RandomStringUtils.randomAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        userRepository.saveAndFlush(user);

        restAccountMockMvc
            .perform(
                post("/api/account/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, "")))
            )
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin("change-password-empty").orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
    }

    @Test
    @Transactional
    @WithUnauthenticatedMockUser
    void testRequestPasswordReset() throws Exception {
        User user = createActivatedUser("password-reset", "password-reset@example.com");
        user.setLangKey("en");
        userRepository.saveAndFlush(user);

        restAccountMockMvc
            .perform(post("/api/account/reset-password/init").contentType(MediaType.TEXT_PLAIN).content("password-reset@example.com"))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
    }

    @Test
    @Transactional
    @WithUnauthenticatedMockUser
    void testRequestPasswordResetUpperCaseEmail() throws Exception {
        User user = createActivatedUser("password-reset-upper-case", "password-reset-upper-case@example.com");
        user.setLangKey("en");
        userRepository.saveAndFlush(user);

        restAccountMockMvc
            .perform(post("/api/account/reset-password/init").contentType(MediaType.TEXT_PLAIN).content("password-reset-upper-case@EXAMPLE.COM"))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
    }

    @Test
    @WithUnauthenticatedMockUser
    void testRequestPasswordResetWrongEmailDoesNotRevealAnything() throws Exception {
        restAccountMockMvc
            .perform(post("/api/account/reset-password/init").contentType(MediaType.TEXT_PLAIN).content("password-reset-wrong-email@example.com"))
            .andExpect(status().isOk())
            .andExpect(content().string(""))
            .andExpect(header().doesNotExist("X-medPortalApp-error"))
            .andExpect(header().doesNotExist("X-medPortalApp-params"));
    }

    @Test
    @Transactional
    @WithUnauthenticatedMockUser
    void testFinishPasswordReset() throws Exception {
        User user = createActivatedUser("finish-password-reset", "finish-password-reset@example.com");
        user.setResetDate(Instant.now().plusSeconds(60));
        user.setResetKey("abcdefghijklmnopqrst");
        userRepository.saveAndFlush(user);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(user.getResetKey());
        keyAndPassword.setNewPassword("Password1!");

        restAccountMockMvc
            .perform(
                post("/api/account/reset-password/finish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(keyAndPassword))
            )
            .andExpect(status().isNoContent())
            .andExpect(content().string(""))
            .andExpect(header().doesNotExist("X-medPortalApp-error"))
            .andExpect(header().doesNotExist("X-medPortalApp-params"));

        User updatedUser = userRepository.findOneByLogin(user.getLogin()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedUser.getPassword())).isTrue();
        assertThat(updatedUser.getResetKey()).isNull();
        assertThat(updatedUser.getResetDate()).isNull();
    }

    @Test
    @Transactional
    @WithUnauthenticatedMockUser
    void testFinishPasswordResetTooSmall() throws Exception {
        User user = createActivatedUser("finish-password-reset-too-small", "finish-password-reset-too-small@example.com");
        user.setResetDate(Instant.now().plusSeconds(60));
        user.setResetKey("shortkey");
        userRepository.saveAndFlush(user);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(user.getResetKey());
        keyAndPassword.setNewPassword("foo");

        restAccountMockMvc
            .perform(
                post("/api/account/reset-password/finish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(keyAndPassword))
            )
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin(user.getLogin()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedUser.getPassword())).isFalse();
    }

    @Test
    @Transactional
    @WithUnauthenticatedMockUser
    void testFinishPasswordResetWrongKeyDoesNotChangePassword() throws Exception {
        User user = createActivatedUser("finish-password-reset-wrong-key", "finish-password-reset-wrong-key@example.com");
        String originalPassword = passwordEncoder.encode("old password");
        user.setPassword(originalPassword);
        user.setResetDate(Instant.now());
        user.setResetKey("realresetkey12345678");
        userRepository.saveAndFlush(user);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey("wrongresetkey1234567");
        keyAndPassword.setNewPassword("Password1!");

        restAccountMockMvc
            .perform(
                post("/api/account/reset-password/finish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(keyAndPassword))
            )
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin("finish-password-reset-wrong-key").orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(passwordEncoder.matches("Password1!", updatedUser.getPassword())).isFalse();
        assertThat(updatedUser.getPassword()).isEqualTo(originalPassword);
        assertThat(updatedUser.getResetKey()).isEqualTo("realresetkey12345678");
    }

    @Test
    @Transactional
    @WithUnauthenticatedMockUser
    void testFinishPasswordResetWithLoginAsKeyDoesNotChangePassword() throws Exception {
        User user = createActivatedUser("admin-reset-key-test", "admin-reset-key-test@example.com");
        String originalPassword = passwordEncoder.encode("old password");
        user.setPassword(originalPassword);
        user.setResetKey("realresetkeyadmin123"); // 20 chars
        user.setResetDate(Instant.now());
        userRepository.saveAndFlush(user);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey("adminresetkeytest123");
        keyAndPassword.setNewPassword("Password1!");

        restAccountMockMvc
            .perform(
                post("/api/account/reset-password/finish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(keyAndPassword))
            )
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin("admin-reset-key-test").orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(passwordEncoder.matches("Password1!", updatedUser.getPassword())).isFalse();
        assertThat(updatedUser.getPassword()).isEqualTo(originalPassword);
        assertThat(updatedUser.getResetKey()).isEqualTo("realresetkeyadmin123");
    }

    private ManagedUserVM createManagedUser(String login, String email, String password) {
        ManagedUserVM user = new ManagedUserVM();
        user.setLogin(login);
        user.setPassword(password);
        user.setFirstName("Alice");
        user.setLastName("Test");
        user.setEmail(email);
        user.setImageUrl("http://placehold.it/50x50");
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        user.setAuthorities(Set.of(AuthoritiesConstants.USER));
        return user;
    }

    private User createActivatedUser(String login, String email) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setPassword(RandomStringUtils.randomAlphanumeric(60));
        user.setActivated(true);
        return user;
    }

    private Authority getAuthority(String authorityName) {
        return authorityRepository
            .findByName(authorityName)
            .orElseThrow(() -> new IllegalStateException(authorityName + " authority was not found in database"));
    }
}
