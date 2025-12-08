package com.scm.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FormLoginSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FormLoginSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            LOGGER.info("User logged in Successfully: {}", userDetails.getUsername());
            new DefaultRedirectStrategy().sendRedirect(request, response, "/user/dashboard");

        } catch (Exception e) {
            LOGGER.error("Unexpected error during user login with formlogin : {}", e);
            new DefaultRedirectStrategy().sendRedirect(request, response, "/login");
        }
    }

}
