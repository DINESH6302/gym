package com.opengym.filter;

import com.opengym.model.User;
import com.opengym.service.UserService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenAuthFilter extends OncePerRequestFilter {

    public static final String USER_ATTR = "authed_user";
    private final UserService userService;

    public TokenAuthFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String jwt = extractJwt(request);
        if (jwt != null) {
            userService.authenticateJwt(jwt).ifPresent(user ->
                request.setAttribute(USER_ATTR, user)
            );
        }
        chain.doFilter(request, response);
    }

    private String extractJwt(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        return null;
    }

    public static User getUser(HttpServletRequest request) {
        return (User) request.getAttribute(USER_ATTR);
    }
}
