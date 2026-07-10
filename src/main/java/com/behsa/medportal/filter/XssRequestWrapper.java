package com.behsa.medportal.filter;

import org.owasp.html.PolicyFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Map;

public class XssRequestWrapper extends HttpServletRequestWrapper {

    private final PolicyFactory policy;

    public XssRequestWrapper(HttpServletRequest request, PolicyFactory policy) {
        super(request);
        this.policy = policy;
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (value != null) {
            return policy.sanitize(value);
        }
        return null;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                values[i] = policy.sanitize(values[i]);
            }
        }
        return values;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = super.getParameterMap();
        map.forEach((k, v) -> {
            for (int i = 0; i < v.length; i++) {
                v[i] = policy.sanitize(v[i]);
            }
        });
        return map;
    }
}
