package com.scm.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.scm.entities.User;
import com.scm.enums.Providers;
import com.scm.repo.UserRepository;
import com.scm.utils.constants.ResponseMessage;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class Oauth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger LOGGER = LoggerFactory.getLogger(Oauth2AuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        LOGGER.info("Inside login request Oauth2AuthenticationSuccessHandler: ");

        try {
            // Save data to database
            // Fetching Provider
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            String providerName = token.getAuthorizedClientRegistrationId();

            // Fetching User details from Google OAuth2Client
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

            Providers provider = providerName.equalsIgnoreCase(Providers.GOOGLE.toString()) ? Providers.GOOGLE
                    : Providers.GITHUB;
            LOGGER.info("provider :{}", provider);

            String providerId = null;
            String name = null;
            String email = null;
            String profilePic = null;

            if (provider == Providers.GOOGLE) {
                providerId = oauth2User.getName();
                name = oauth2User.getAttribute("name");
                email = oauth2User.getAttribute("email");
                profilePic = oauth2User.getAttribute("picture");
            } else if (provider == Providers.GITHUB) {
                providerId = oauth2User.getName();
                name = oauth2User.getAttribute("name");
                String userEmail = oauth2User.getAttribute("email");
                email = userEmail != null ? userEmail : (oauth2User.getAttribute("login") + "@gmail.com");
                profilePic = oauth2User.getAttribute("avatar_url");
            } else {
                LOGGER.error("Provider is not available: {}", provider);
                request.getSession().setAttribute("message",
                        "Something went wrong, Connect with LinkHub(SCM) tech team");
                new DefaultRedirectStrategy().sendRedirect(request, response, "/login");
                return;
            }

            // oauth2User.getAttributes().forEach((k, v) -> LOGGER.info(k + " : " + v));

            // Create user
            User user = User.builder()
                    .userId(UUID.randomUUID().toString())
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode("Password"))
                    .profilePic(profilePic)
                    .about("User logged in through " + provider)
                    .roles(List.of(ResponseMessage.ROLE_USER))
                    .enabled(true)
                    .emailVerified(true)
                    .provider(provider)
                    .providerId(providerId)
                    .build();

            Optional<User> savedUser = userRepository.findByEmail(email);

            if (!savedUser.isPresent()) {
                userRepository.save(user);
                LOGGER.info("User Registered Successfully Using {} OAuth2client, email:{}", provider, email);
            }

            LOGGER.info("User logged in Successfully.");
            new DefaultRedirectStrategy().sendRedirect(request, response, "/user/dashboard");

        } catch (Exception e) {
            LOGGER.error("Unexpected error during user login with OAuth2Client: {}", e);
            request.getSession().setAttribute("message", "Something went wrong, Connect with LinkHub(SCM) tech team");
            new DefaultRedirectStrategy().sendRedirect(request, response, "/login");
        }

    }

}
