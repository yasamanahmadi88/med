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
