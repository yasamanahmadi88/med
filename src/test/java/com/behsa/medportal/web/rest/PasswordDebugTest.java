package com.behsa.medportal.web.rest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
/**
 * Utility class for testing REST controllers.
 */
//@SpringBootTest
public final class PasswordDebugTest {


    @Test
    void checkAndGenerateAdminPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String currentHash = "$2a$10$7oW4F/5BNW3iEfEbc.WjRuI5cYeJsMBRX9I7j42H7AjHcpEbLUAaC";

        System.out.println("CURRENT HASH MATCHES admin = " + encoder.matches("admin", currentHash));

        String newHash = encoder.encode("admin");

        System.out.println("NEW HASH FOR admin = " + newHash);
        System.out.println("NEW HASH MATCHES admin = " + encoder.matches("admin", newHash));
    }
}
