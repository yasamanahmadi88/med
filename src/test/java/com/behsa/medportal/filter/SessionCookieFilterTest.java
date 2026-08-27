package com.behsa.medportal.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SessionCookieFilterTest {

    @Test
    void testSessionCookieFilter() throws ServletException, IOException {
        // Create the filter
        SessionCookieFilter filter = new SessionCookieFilter();
        
        // Create mock request and response
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        
        // Set up request with session cookie
        request.setCookies(new jakarta.servlet.http.Cookie("JSESSIONID", "test-session-id"));
        request.setRequestURI("/api/test");
        
        // Execute the filter
        filter.doFilter(request, response, filterChain);
        
        // Verify the filter chain was called
        assertTrue(filterChain.getRequest() != null);
        assertTrue(filterChain.getResponse() != null);
    }
} 