package com.behsa.medportal.filter;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class XssSanitizingFilter implements Filter {

    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    /**
     * Filter HTTP requests to sanitize XSS payloads in request parameters and body.
     * Applies HTML sanitization using OWASP HTML Sanitizer for formatting and links.
     * Covers all HTTP methods that may accept user input (POST, PUT, PATCH, GET, DELETE, HEAD).
     * CWE-79: Improper Neutralization of Input During Web Page Generation.
     *
     * @param request HTTP request to filter
     * @param response HTTP response
     * @param chain filter chain to continue processing
     * @throws IOException if an I/O error occurs during filtering
     * @throws ServletException if a servlet error occurs during filtering
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String method = httpRequest.getMethod().toUpperCase();

        // Apply XSS sanitization to all HTTP methods that accept user input
        if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method) ||
            "GET".equals(method) || "DELETE".equals(method) || "HEAD".equals(method)) {
            chain.doFilter(new XssRequestWrapper(httpRequest, policy), response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
