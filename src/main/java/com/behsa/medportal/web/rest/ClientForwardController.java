package com.behsa.medportal.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ClientForwardController {

    /**
     * Forwards frontend routes to the client index.html.
     *
     * Backend/API/management/documentation paths must not be forwarded to Angular,
     * otherwise unknown API URLs return 200 with index.html instead of a real 404.
     *
     * @param request the current HTTP request
     * @return forward to client index.html
     */
    @GetMapping(value = "/**/{path:[^\\.]*}")
    public String forward(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (isBackendOrInfrastructurePath(uri)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return "forward:/";
    }

    private boolean isBackendOrInfrastructurePath(String uri) {
        return uri.equals("/api")
            || uri.startsWith("/api/")
            || uri.equals("/management")
            || uri.startsWith("/management/")
            || uri.equals("/v3")
            || uri.startsWith("/v3/")
            || uri.equals("/swagger-ui")
            || uri.startsWith("/swagger-ui/")
            || uri.equals("/swagger-resources")
            || uri.startsWith("/swagger-resources/")
            || uri.equals("/api-docs")
            || uri.startsWith("/api-docs/");
    }
}
