package com.behsa.medportal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.behsa.medportal.domain.enumeration.Verb;
import com.behsa.medportal.security.AuthoritiesConstants;
import com.behsa.medportal.service.ResourceAuthorityQueryService;
import com.behsa.medportal.service.dto.ResourceAuthorityDTO;
import com.behsa.medportal.service.dto.ResourceDTO;
import java.lang.reflect.Method;
import java.util.List;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;

class ResourceSecuredAuthorizationManagerTest {

    private ResourceAuthorityQueryService resourceAuthorityQueryService;
    private ResourceSecuredAuthorizationManager authorizationManager;

    @BeforeEach
    void setup() {
        resourceAuthorityQueryService = mock(ResourceAuthorityQueryService.class);
        authorizationManager = new ResourceSecuredAuthorizationManager(resourceAuthorityQueryService);
    }

    @Test
    void permitsMethodsWithoutSecuredAnnotation() throws Exception {
        assertThat(authorizes("open", authenticated(AuthoritiesConstants.USER))).isTrue();
    }

    @Test
    void deniesRoleSecuredMethodWhenRoleIsMissing() throws Exception {
        assertThat(authorizes("adminOnly", authenticated(AuthoritiesConstants.USER))).isFalse();
    }

    @Test
    void permitsRoleSecuredMethodWhenRoleMatches() throws Exception {
        assertThat(authorizes("adminOnly", authenticated(AuthoritiesConstants.ADMIN))).isTrue();
    }

    @Test
    void permitsResourceSecuredMethodWhenVerbGrantMatches() throws Exception {
        when(resourceAuthorityQueryService.findByAuthorities(anyList(), any(Pageable.class))).thenReturn(
            new PageImpl<>(List.of(resourceAuthority("patient-chart", Verb.VIEW)))
        );

        assertThat(authorizes("viewPatientChart", authenticated(AuthoritiesConstants.USER))).isTrue();
    }

    @Test
    void deniesResourceSecuredMethodWhenGrantIsInsufficient() throws Exception {
        when(resourceAuthorityQueryService.findByAuthorities(anyList(), any(Pageable.class))).thenReturn(
            new PageImpl<>(List.of(resourceAuthority("patient-chart", Verb.CREATE)))
        );

        assertThat(authorizes("viewPatientChart", authenticated(AuthoritiesConstants.USER))).isFalse();
    }

    @Test
    void deniesResourceSecuredMethodWhenHttpVerbCannotBeResolved() throws Exception {
        when(resourceAuthorityQueryService.findByAuthorities(anyList(), any(Pageable.class))).thenReturn(
            new PageImpl<>(List.of(resourceAuthority("patient-chart", Verb.VIEW)))
        );

        assertThat(authorizes("resourceWithoutHttpMapping", authenticated(AuthoritiesConstants.USER))).isFalse();
    }

    @Test
    void deniesAnonymousAuthenticationForSecuredMethods() throws Exception {
        Authentication anonymous = new AnonymousAuthenticationToken(
            "key",
            "anonymous",
            List.of(new SimpleGrantedAuthority(AuthoritiesConstants.ANONYMOUS))
        );

        assertThat(authorizes("adminOnly", anonymous)).isFalse();
    }

    private boolean authorizes(String methodName, Authentication authentication) throws Exception {
        return authorizationManager.authorize(() -> authentication, invocation(methodName)).isGranted();
    }

    private MethodInvocation invocation(String methodName) throws Exception {
        MethodInvocation methodInvocation = mock(MethodInvocation.class);
        Method method = TestController.class.getDeclaredMethod(methodName);
        when(methodInvocation.getMethod()).thenReturn(method);
        return methodInvocation;
    }

    private Authentication authenticated(String authority) {
        return new UsernamePasswordAuthenticationToken(
            "user",
            "password",
            List.of(new SimpleGrantedAuthority(authority))
        );
    }

    private ResourceAuthorityDTO resourceAuthority(String resourceName, Verb verb) {
        ResourceDTO resource = new ResourceDTO();
        resource.setName(resourceName);

        ResourceAuthorityDTO resourceAuthority = new ResourceAuthorityDTO();
        resourceAuthority.setResource(resource);
        resourceAuthority.setVerb(verb);
        return resourceAuthority;
    }

    static class TestController {

        void open() {}

        @Secured(AuthoritiesConstants.ADMIN)
        void adminOnly() {}

        @Secured("patient-chart")
        @GetMapping("/patient-chart")
        void viewPatientChart() {}

        @Secured("patient-chart")
        void resourceWithoutHttpMapping() {}
    }
}
