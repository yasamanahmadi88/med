package com.behsa.medportal.web.rest;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.Authority;
import com.behsa.medportal.domain.User;
import com.behsa.medportal.repository.AuthorityRepository;
import com.behsa.medportal.repository.UserRepository;
import com.behsa.medportal.security.AuthoritiesConstants;
import com.behsa.medportal.security.captcha.CaptchaValidationService;
import com.behsa.medportal.security.jwt.JWTFilter;
import com.behsa.medportal.web.rest.vm.KeyAndPasswordVM;
import com.behsa.medportal.web.rest.vm.LoginVM;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@AutoConfigureMockMvc
@IntegrationTest
class AuthResourceIT {

    private static final String AUTHENTICATE_URL = "/api/authenticate";

    private static final String TEST_CAPTCHA_ID = "test-captcha-id";

    private static final String TEST_CAPTCHA_TOKEN = "test-captcha-token";

    @Autowired
    private MockMvc restAuthMockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private CaptchaValidationService captchaValidationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Transactional
    @WithUnauthenticatedMockUser
    void testAuthenticateDoesNotRevealExistingUsername() throws Exception {
        doNothing()
            .when(captchaValidationService)
            .validate(anyString(), anyString(), anyString());

        createActivatedUser(
            "existing-auth-user",
            "existing-auth-user@example.com",
            "correct-password"
        );

        LoginVM existingUsernameWrongPassword = createLoginVM(
            "existing-auth-user",
            "wrong-password"
        );

        LoginVM nonExistingUsernameWrongPassword = createLoginVM(
            "non-existing-auth-user",
            "wrong-password"
        );

        MvcResult existingUsernameResult = performFailedLogin(existingUsernameWrongPassword);

        MvcResult nonExistingUsernameResult = performFailedLogin(nonExistingUsernameWrongPassword);

        assertThat(nonExistingUsernameResult.getResponse().getStatus())
            .isEqualTo(existingUsernameResult.getResponse().getStatus());

        assertThat(nonExistingUsernameResult.getResponse().getContentAsString())
            .isEqualTo(existingUsernameResult.getResponse().getContentAsString());

        assertThat(nonExistingUsernameResult.getResponse().getHeaderNames())
            .doesNotContain("X-medPortalApp-error", "X-medPortalApp-params");

        assertThat(existingUsernameResult.getResponse().getHeaderNames())
            .doesNotContain("X-medPortalApp-error", "X-medPortalApp-params");
    }

    private MvcResult performFailedLogin(LoginVM loginVM) throws Exception {
        return restAuthMockMvc
            .perform(
                post(AUTHENTICATE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(loginVM))
            )
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(""))
            .andExpect(header().doesNotExist("X-medPortalApp-error"))
            .andExpect(header().doesNotExist("X-medPortalApp-params"))
            .andReturn();
    }

    private LoginVM createLoginVM(String username, String password) {
        LoginVM loginVM = new LoginVM();
        loginVM.setUsername(username);
        loginVM.setPassword(password);
        loginVM.setRememberMe(false);
        loginVM.setCaptchaId(TEST_CAPTCHA_ID);
        loginVM.setCaptchaToken(TEST_CAPTCHA_TOKEN);
        return loginVM;
    }

    private void createActivatedUser(String login, String email, String rawPassword) {
        userRepository.findOneByLogin(login).ifPresent(userRepository::delete);
        userRepository.flush();

        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActivated(true);
        user.setAuthorities(new HashSet<>(Collections.singleton(getAuthority(AuthoritiesConstants.USER))));

        userRepository.saveAndFlush(user);
    }

    private Authority getAuthority(String authorityName) {
        return authorityRepository
            .findByName(authorityName)
            .orElseThrow(() -> new IllegalStateException(authorityName + " authority was not found in database"));
    }


    @Test
    @Transactional
    @WithUnauthenticatedMockUser
    void finishPasswordResetShouldRejectUsernameAsResetKey() throws Exception {
        KeyAndPasswordVM keyAndPasswordVM = new KeyAndPasswordVM();
        keyAndPasswordVM.setKey("admin");
        keyAndPasswordVM.setNewPassword("12345678");

        restAuthMockMvc
            .perform(
                post("/api/account/reset-password/finish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(keyAndPasswordVM))
            )
            .andExpect(status().isBadRequest());
    }
    @Test
    @Transactional
    @WithUnauthenticatedMockUser
    void finishPasswordResetShouldRejectUnknownResetKey() throws Exception {
        KeyAndPasswordVM keyAndPasswordVM = new KeyAndPasswordVM();
        keyAndPasswordVM.setKey("ABCDEFGHIJKLMNOPQRST");
        keyAndPasswordVM.setNewPassword("12345678");

        restAuthMockMvc
            .perform(
                post("/api/account/reset-password/finish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(keyAndPasswordVM))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithUnauthenticatedMockUser
    void tokenShouldBeRejectedAfterUserIsDeactivated() throws Exception {
        doNothing()
            .when(captchaValidationService)
            .validate(anyString(), anyString(), anyString());

        createActivatedUser(
            "disabled-token-user",
            "disabled-token-user@example.com",
            "CorrectPassword123"
        );

        LoginVM loginVM = createLoginVM(
            "disabled-token-user",
            "CorrectPassword123"
        );

        MvcResult loginResult = restAuthMockMvc
            .perform(
                post(AUTHENTICATE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(loginVM))
            )
            .andExpect(status().isOk())
            .andReturn();

        JsonNode jsonNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = jsonNode.get("id_token").asText();

        User user = userRepository
            .findOneByLogin("disabled-token-user")
            .orElseThrow(() -> new IllegalStateException("Test user was not found"));

        user.setActivated(false);
        userRepository.saveAndFlush(user);

        restAuthMockMvc
            .perform(
                get("/api/account")
                    .header(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + token)
            )
            .andExpect(status().isUnauthorized());
    }



}
