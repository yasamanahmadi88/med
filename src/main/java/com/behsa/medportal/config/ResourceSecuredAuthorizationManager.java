package com.behsa.medportal.config;

import com.behsa.medportal.service.ResourceAuthorityQueryService;
import com.behsa.medportal.service.dto.ResourceAuthorityDTO;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Spring Security 6+/7 replacement for {@link CustomAccessDecisionManager}.
 *
 * <p>Preserves MedPortal resource+verb authorization for {@code @Secured("resourceName")} while
 * keeping standard role checks for {@code @Secured("ROLE_*")}.
 */
public class ResourceSecuredAuthorizationManager implements AuthorizationManager<MethodInvocation> {

    private static final Logger log = LoggerFactory.getLogger(ResourceSecuredAuthorizationManager.class);

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String AUTHENTICATED_ATTRIBUTE_PREFIX = "IS_AUTHENTICATED_";

    private static final String VIEW = "VIEW";
    private static final String CREATE = "CREATE";
    private static final String EDIT = "EDIT";
    private static final String DELETE = "DELETE";
    private static final String NO_GRANT = "NO_GRANT";

    private final ResourceAuthorityQueryService resourceAuthorityQueryService;

    public ResourceSecuredAuthorizationManager(ResourceAuthorityQueryService resourceAuthorityQueryService) {
        this.resourceAuthorityQueryService = resourceAuthorityQueryService;
    }

    @Override
    public AuthorizationResult authorize(Supplier<? extends Authentication> authentication, MethodInvocation methodInvocation) {
        Secured secured = resolveSecured(methodInvocation);
        if (secured == null || secured.value().length == 0) {
            return new AuthorizationDecision(true);
        }

        List<String> values = Arrays.stream(secured.value()).filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).toList();

        List<String> roleAttributes = values.stream().filter(v -> !isResourceAttribute(v)).toList();
        List<String> resourceNames = values.stream().filter(this::isResourceAttribute).toList();

        Authentication auth = authentication.get();

        if (!roleAttributes.isEmpty() && !hasAnyAuthority(auth, roleAttributes)) {
            return new AuthorizationDecision(false);
        }

        if (!resourceNames.isEmpty()) {
            return new AuthorizationDecision(decideResourceAccess(auth, methodInvocation, resourceNames));
        }

        // Only role attributes were present and passed (or none after filtering).
        return new AuthorizationDecision(!roleAttributes.isEmpty() || values.isEmpty());
    }

    private Secured resolveSecured(MethodInvocation methodInvocation) {
        Method method = methodInvocation.getMethod();
        Secured secured = method.getAnnotation(Secured.class);
        if (secured != null) {
            return secured;
        }
        return method.getDeclaringClass().getAnnotation(Secured.class);
    }

    private boolean hasAnyAuthority(Authentication authentication, Collection<String> required) {
        if (!isAuthenticated(authentication)) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        return authorities.stream().map(GrantedAuthority::getAuthority).anyMatch(required::contains);
    }

    private boolean decideResourceAccess(Authentication authentication, MethodInvocation methodInvocation, List<String> resourceNames) {
        if (!isAuthenticated(authentication)) {
            return false;
        }

        String verb = resolveVerb(methodInvocation.getMethod());
        if (NO_GRANT.equals(verb)) {
            log.debug(
                "Access denied because no supported HTTP mapping was found for method {}.",
                methodInvocation.getMethod().getName()
            );
            return false;
        }

        List<String> userAuthorities = authentication
            .getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        if (userAuthorities.isEmpty()) {
            return false;
        }

        return hasAccessToAnyResource(userAuthorities, verb, resourceNames);
    }

    private boolean isResourceAttribute(String value) {
        return value != null &&
            !value.isEmpty() &&
            !value.startsWith(ROLE_PREFIX) &&
            !value.startsWith(AUTHENTICATED_ATTRIBUTE_PREFIX);
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null &&
            authentication.isAuthenticated() &&
            !(authentication instanceof AnonymousAuthenticationToken);
    }

    private String resolveVerb(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) {
            return VIEW;
        }
        if (method.isAnnotationPresent(PostMapping.class)) {
            return CREATE;
        }
        if (method.isAnnotationPresent(PutMapping.class) || method.isAnnotationPresent(PatchMapping.class)) {
            return EDIT;
        }
        if (method.isAnnotationPresent(DeleteMapping.class)) {
            return DELETE;
        }

        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null && requestMapping.method().length > 0) {
            RequestMethod requestMethod = requestMapping.method()[0];
            if (RequestMethod.GET.equals(requestMethod)) {
                return VIEW;
            }
            if (RequestMethod.POST.equals(requestMethod)) {
                return CREATE;
            }
            if (RequestMethod.PUT.equals(requestMethod) || RequestMethod.PATCH.equals(requestMethod)) {
                return EDIT;
            }
            if (RequestMethod.DELETE.equals(requestMethod)) {
                return DELETE;
            }
        }
        return NO_GRANT;
    }

    private boolean hasAccessToAnyResource(List<String> userAuthorities, String verb, List<String> resourceNames) {
        List<ResourceAuthorityDTO> resourceAuthorities = resourceAuthorityQueryService
            .findByAuthorities(userAuthorities, Pageable.unpaged())
            .getContent();

        return resourceAuthorities
            .stream()
            .filter(Objects::nonNull)
            .filter(resourceAuthority -> resourceAuthority.getResource() != null)
            .filter(resourceAuthority -> resourceAuthority.getResource().getName() != null)
            .filter(resourceAuthority -> resourceAuthority.getVerb() != null)
            .anyMatch(
                resourceAuthority ->
                    resourceNames
                        .stream()
                        .anyMatch(
                            resourceName ->
                                resourceName.equalsIgnoreCase(resourceAuthority.getResource().getName()) &&
                                verb.equalsIgnoreCase(resourceAuthority.getVerb().name())
                        )
            );
    }
}
