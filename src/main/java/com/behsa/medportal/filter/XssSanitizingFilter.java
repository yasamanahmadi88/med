package com.behsa.medportal.filter;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class XssSanitizingFilter implements Filter {

    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if ("POST".equalsIgnoreCase(httpRequest.getMethod()) ||
            "PUT".equalsIgnoreCase(httpRequest.getMethod()) ||
            "PATCH".equalsIgnoreCase(httpRequest.getMethod())) {

            chain.doFilter(new XssRequestWrapper(httpRequest, policy), response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
