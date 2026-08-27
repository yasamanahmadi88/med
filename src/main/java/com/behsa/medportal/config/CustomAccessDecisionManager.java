package com.behsa.medportal.config;

import com.behsa.medportal.service.ResourceAuthorityQueryService;
import com.behsa.medportal.service.dto.ResourceAuthorityDTO;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Custom AccessDecisionManager for method security.
 *
 * This class keeps Spring Security's default method-security behavior and adds
 * application-specific resource permission checks.
 *
 * Spring Security default checks handle:
 * - @PreAuthorize(...)
 * - @Secured("ROLE_ADMIN")
 * - authenticated / role-based attributes
 *
 * Custom resource checks handle:
 * - @Secured("config")
 * - @Secured("resource")
 * - @Secured("resourceAuthority")
 * - @Secured("flow")
 * - any other resource name stored in jhi_resource.name
 */
public class CustomAccessDecisionManager implements AccessDecisionManager {

    private static final Logger log = LoggerFactory.getLogger(CustomAccessDecisionManager.class);

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String AUTHENTICATED_ATTRIBUTE_PREFIX = "IS_AUTHENTICATED_";

    private static final String VIEW = "VIEW";
    private static final String CREATE = "CREATE";
    private static final String EDIT = "EDIT";
    private static final String DELETE = "DELETE";
    private static final String NO_GRANT = "NO_GRANT";

    private final AccessDecisionManager defaultAccessDecisionManager;

    private final ResourceAuthorityQueryService resourceAuthorityQueryService;

    public CustomAccessDecisionManager(
        AccessDecisionManager defaultAccessDecisionManager,
        ResourceAuthorityQueryService resourceAuthorityQueryService
    ) {
        this.defaultAccessDecisionManager = defaultAccessDecisionManager;
        this.resourceAuthorityQueryService = resourceAuthorityQueryService;
    }

    @Override
    public void decide(
        Authentication authentication,
        Object object,
        Collection<ConfigAttribute> configAttributes
    ) throws AccessDeniedException, InsufficientAuthenticationException {

        if (configAttributes == null || configAttributes.isEmpty()) {
            return;
        }

        List<ConfigAttribute> defaultAttributes = configAttributes
            .stream()
            .filter(attribute -> !isResourceAttribute(attribute))
            .collect(Collectors.toList());

        List<String> resourceNames = configAttributes
            .stream()
            .filter(this::isResourceAttribute)
            .map(this::getAttributeValue)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        /*
         * First, let Spring Security handle its standard checks.
         *
         * This preserves:
         * - @PreAuthorize("hasAuthority('ROLE_ADMIN')")
         * - @Secured("ROLE_ADMIN")
         * - other standard Spring Security method rules
         */
        if (!defaultAttributes.isEmpty()) {
            defaultAccessDecisionManager.decide(authentication, object, defaultAttributes);
        }

        /*
         * Then apply project-specific resource permission checks.
         *
         * This means when both @PreAuthorize and @Secured("resourceName") exist,
         * both must pass.
         */
        if (!resourceNames.isEmpty()) {
            if (!(object instanceof MethodInvocation)) {
                throw new AccessDeniedException("Unsupported secured object type for resource permission check.");
            }

            MethodInvocation methodInvocation = (MethodInvocation) object;
            decideResourceAccess(authentication, methodInvocation, resourceNames);
        }
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return isResourceAttribute(attribute) || defaultAccessDecisionManager.supports(attribute);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return MethodInvocation.class.isAssignableFrom(clazz) || defaultAccessDecisionManager.supports(clazz);
    }

    private void decideResourceAccess(
        Authentication authentication,
        MethodInvocation methodInvocation,
        List<String> resourceNames
    ) {
        if (!isAuthenticated(authentication)) {
            throw new InsufficientAuthenticationException("Authentication is required to access this resource.");
        }

        String verb = resolveVerb(methodInvocation.getMethod());

        if (NO_GRANT.equals(verb)) {
            log.debug(
                "Access denied because no supported HTTP mapping was found for method {}.",
                methodInvocation.getMethod().getName()
            );

            throw new AccessDeniedException("No supported HTTP mapping was found for resource permission check.");
        }

        List<String> userAuthorities = authentication
            .getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        if (userAuthorities.isEmpty()) {
            throw new AccessDeniedException("User has no authorities.");
        }

        boolean hasAccess = hasAccessToAnyResource(userAuthorities, verb, resourceNames);

        if (!hasAccess) {
            throw new AccessDeniedException(
                "User does not have enough privileges. Required verb: " +
                    verb +
                    ", resources: " +
                    resourceNames
            );
        }
    }

    private boolean isResourceAttribute(ConfigAttribute attribute) {
        String value = getAttributeValue(attribute);

        return value != null
            && !value.trim().isEmpty()
            && !value.startsWith(ROLE_PREFIX)
            && !value.startsWith(AUTHENTICATED_ATTRIBUTE_PREFIX);
    }

    private String getAttributeValue(ConfigAttribute attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            return attribute.getAttribute();
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
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

    private boolean hasAccessToAnyResource(
        List<String> userAuthorities,
        String verb,
        List<String> resourceNames
    ) {
        List<ResourceAuthorityDTO> resourceAuthorities = resourceAuthorityQueryService
            .findByAuthorities(userAuthorities, Pageable.unpaged())
            .getContent();

        return resourceAuthorities
            .stream()
            .filter(Objects::nonNull)
            .filter(resourceAuthority -> resourceAuthority.getResource() != null)
            .filter(resourceAuthority -> resourceAuthority.getResource().getName() != null)
            .filter(resourceAuthority -> resourceAuthority.getVerb() != null)
            .anyMatch(resourceAuthority ->
                resourceNames
                    .stream()
                    .anyMatch(resourceName ->
                        resourceName.equalsIgnoreCase(resourceAuthority.getResource().getName())
                            && verb.equalsIgnoreCase(resourceAuthority.getVerb().name())
                    )
            );
    }
}
