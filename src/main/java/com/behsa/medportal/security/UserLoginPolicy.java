package com.behsa.medportal.security;



import com.behsa.medportal.web.rest.errors.BadRequestAlertException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class UserLoginPolicy {

    private static final String ENTITY_NAME = "userManagement";

    private static final Set<String> RESERVED_LOGINS =
        Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "admin",
            "administrator",
            "root",
            "system",
            "superuser",
            "support",
            "security",
            "operator"
        )));

    public String normalize(String login) {
        if (login == null) {
            return null;
        }

        return Normalizer
            .normalize(login.trim(), Normalizer.Form.NFKC)
            .toLowerCase(Locale.ROOT);
    }

    public String validateAndNormalizeForNewUser(String login) {
        String normalizedLogin = normalize(login);

        if (StringUtils.isBlank(normalizedLogin)) {
            throw new BadRequestAlertException(
                "Login is required",
                ENTITY_NAME,
                "loginrequired"
            );
        }

        if (RESERVED_LOGINS.contains(normalizedLogin)) {
            throw new BadRequestAlertException(
                "This login is reserved and cannot be used",
                ENTITY_NAME,
                "loginreserved"
            );
        }

        return normalizedLogin;
    }
}
