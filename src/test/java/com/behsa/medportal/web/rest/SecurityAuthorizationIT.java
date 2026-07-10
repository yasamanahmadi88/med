package com.behsa.medportal.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.User;
import com.behsa.medportal.repository.UserRepository;
import com.behsa.medportal.security.AuthoritiesConstants;
import com.behsa.medportal.security.jwt.TokenProvider;
import com.behsa.medportal.web.rest.vm.LoginVM;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.behsa.medportal.security.PortalUser;

/**
 * Security integration coverage for authn/authz, CORS, and actuator exposure.
 */
@AutoConfigureMockMvc
@IntegrationTest
class SecurityAuthorizationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;

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

    @Test
    void expiredTokenIsUnauthorized() throws Exception {
        // Empty / garbage bearer
        mockMvc
            .perform(get("/api/account").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
            .andExpect(status().isUnauthorized());
    }

    private String tokenFor(String login, String authority) {
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
        return tokenProvider.createToken(new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities()), false);
    }
}
