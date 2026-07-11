package com.behsa.medportal.web.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.Authority;
import com.behsa.medportal.domain.User;
import com.behsa.medportal.repository.AuthorityRepository;
import com.behsa.medportal.repository.UserRepository;
import com.behsa.medportal.security.AuthoritiesConstants;
import com.behsa.medportal.security.SecurityCache;
import com.behsa.medportal.security.PortalUser;
import com.behsa.medportal.security.captcha.CaptchaValidationService;
import com.behsa.medportal.security.jwt.TokenProvider;
import com.behsa.medportal.web.rest.vm.LoginVM;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Security integration coverage for authn/authz, CORS, and actuator exposure.
 */
@AutoConfigureMockMvc
@IntegrationTest
class SecurityAuthorizationIT {

    private static final String TEST_CAPTCHA_ID = "security-authorization-it-captcha-id";
    private static final String TEST_CAPTCHA_TOKEN = "security-authorization-it-captcha-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private SecurityCache securityCache;

    @MockitoBean
    private CaptchaValidationService captchaValidationService;

    @BeforeEach
    void stubCaptcha() {
        doNothing().when(captchaValidationService).validate(anyString(), anyString(), anyString());
    }

    @Test
    void missingTokenReturnsUnauthorizedForProtectedApi() throws Exception {
        mockMvc.perform(get("/api/account")).andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void wrongPasswordReturnsUnauthorized() throws Exception {
        User user = new User();
        user.setLogin("sec-wrong-pass");
        user.setEmail("sec-wrong-pass@example.com");
        user.setActivated(true);
        user.setPassword(passwordEncoder.encode("correct"));
        userRepository.saveAndFlush(user);

        LoginVM login = new LoginVM();
        login.setUsername("sec-wrong-pass");
        login.setPassword("incorrect");
        login.setCaptchaId(TEST_CAPTCHA_ID);
        login.setCaptchaToken(TEST_CAPTCHA_TOKEN);
        mockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(login)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.id_token").doesNotExist());
    }

    @Test
    @Transactional
    void inactiveUserCannotAuthenticate() throws Exception {
        User user = new User();
        user.setLogin("sec-inactive");
        user.setEmail("sec-inactive@example.com");
        user.setActivated(false);
        user.setPassword(passwordEncoder.encode("test"));
        userRepository.saveAndFlush(user);

        LoginVM login = new LoginVM();
        login.setUsername("sec-inactive");
        login.setPassword("test");
        login.setCaptchaId(TEST_CAPTCHA_ID);
        login.setCaptchaToken(TEST_CAPTCHA_TOKEN);
        mockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(login)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void userTokenCannotAccessAdminApi() throws Exception {
        String token = tokenFor("user", AuthoritiesConstants.USER);
        mockMvc
            .perform(get("/api/admin/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminTokenCanAccessAdminApi() throws Exception {
        String token = tokenFor("admin", AuthoritiesConstants.ADMIN);
        mockMvc
            .perform(get("/api/admin/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void actuatorHealthRequiresAdmin() throws Exception {
        mockMvc.perform(get("/management/health")).andExpect(status().isUnauthorized());

        String userToken = tokenFor("user", AuthoritiesConstants.USER);
        mockMvc
            .perform(get("/management/health").header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void malformedBearerTokenReturnsUnauthorized() throws Exception {
        mockMvc
            .perform(get("/api/account").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void authorizationHeaderWithoutBearerReturnsUnauthorized() throws Exception {
        String token = tokenFor("user-no-bearer", AuthoritiesConstants.USER);

        mockMvc.perform(get("/api/account").header(HttpHeaders.AUTHORIZATION, token)).andExpect(status().isUnauthorized());
    }

    @Test
    void emptyAuthorizationHeaderReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/account").header(HttpHeaders.AUTHORIZATION, "")).andExpect(status().isUnauthorized());
    }

    @Test
    void queryStringTokenIsRejected() throws Exception {
        String token = tokenFor("query-token-user", AuthoritiesConstants.USER);

        mockMvc.perform(get("/api/account").param("access_token", token)).andExpect(status().isUnauthorized());
    }

    @Test
    void corsPreflightAllowsConfiguredOrigin() throws Exception {
        mockMvc
            .perform(
                options("/api/authenticate")
                    .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type,authorization")
            )
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
    }

    @Test
    void corsPreflightRejectsUnknownOrigin() throws Exception {
        mockMvc
            .perform(
                options("/api/authenticate")
                    .header(HttpHeaders.ORIGIN, "https://evil.example")
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
            )
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    private String tokenFor(String login, String authority) {
        ensureActiveUser(login, authority);

        PortalUser principal = new PortalUser(
            login,
            "",
            true,
            true,
            true,
            true,
            List.of(new SimpleGrantedAuthority(authority)),
            "party",
            List.of(),
            null
        );
        String token = tokenProvider.createToken(new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities()), false);
        securityCache.storeSession(
            principal,
            "session-" + login,
            "127.0.0.1",
            login,
            token,
            "MockMvc",
            LocalDateTime.now(),
            null,
            true
        );
        return token;
    }

    private void ensureActiveUser(String login, String authorityName) {
        User user = userRepository.findOneByLogin(login).orElseGet(User::new);
        user.setLogin(login);
        user.setEmail(login + "@example.com");
        user.setActivated(true);
        user.setPassword(passwordEncoder.encode("test-password"));
        user.setPartyId("party");
        user.setAuthorities(new HashSet<>(Set.of(getAuthority(authorityName))));
        userRepository.saveAndFlush(user);
    }

    private Authority getAuthority(String authorityName) {
        return authorityRepository
            .findByName(authorityName)
            .orElseThrow(() -> new IllegalStateException(authorityName + " authority was not found in database"));
    }
}
