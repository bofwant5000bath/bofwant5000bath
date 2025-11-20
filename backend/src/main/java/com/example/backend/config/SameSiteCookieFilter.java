package com.example.backend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class SameSiteCookieFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        chain.doFilter(request, response);

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String header = httpResponse.getHeader("Set-Cookie");
        if (header != null && header.contains("JSESSIONID")) {
            httpResponse.setHeader("Set-Cookie",
                header + "; Secure; HttpOnly; SameSite=None");
        }
    }
}
