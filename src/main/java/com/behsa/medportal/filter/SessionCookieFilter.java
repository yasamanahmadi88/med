package com.behsa.medportal.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class SessionCookieFilter extends GenericFilterBean {

    private final List<String> PATHS_TO_IGNORE_SETTING_SAMESITE = Arrays.asList("resources", "home");
    private static final String SESSION_COOKIE_NAME = "JSESSIONID";
    private static final String SESSION_PATH_ATTRIBUTE = ";Path=";
    private static final String ROOT_CONTEXT = "/";
    private static final String SAME_SITE_ATTRIBUTE_VALUES = ";SameSite=Strict;HttpOnly;Secure";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String requestUrl = req.getRequestURL().toString();
        boolean isResourceRequest = StringUtils.isNotBlank(
            PATHS_TO_IGNORE_SETTING_SAMESITE.stream().filter(s -> requestUrl.contains(s)).findFirst().orElse(null)
        );
        if (!isResourceRequest) {
            Cookie[] cookies = ((HttpServletRequest) request).getCookies();
            if (cookies != null && cookies.length > 0) {
                List<Cookie> cookieList = Arrays.asList(cookies);
                Cookie sessionCookie = cookieList
                    .stream()
                    .filter(cookie -> SESSION_COOKIE_NAME.equals(cookie.getName()))
                    .findFirst()
                    .orElse(null);
                if (sessionCookie != null) {
                    String contextPath = request.getServletContext() != null &&
                        StringUtils.isNotBlank(request.getServletContext().getContextPath())
                        ? request.getServletContext().getContextPath()
                        : ROOT_CONTEXT;
                    resp.addHeader(
                        HttpHeaders.SET_COOKIE,
                        sessionCookie.getName() +
                        "=" +
                        sessionCookie.getValue() +
                        SESSION_PATH_ATTRIBUTE +
                        contextPath +
                        SAME_SITE_ATTRIBUTE_VALUES
                    );
                }
            }
        }
        chain.doFilter(request, response);
    }
} 