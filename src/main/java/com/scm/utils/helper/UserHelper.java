package com.scm.utils.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.scm.entities.User;
import com.scm.enums.Providers;

public class UserHelper {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserHelper.class);

    public static String getLoggedInUserEmail(Authentication authentication) {
        String username = null;
        try {
            // LOGGER.info("Inside UserHelper -> getLoggedInUserEmail.");
            // User logged in through OAuth2
            if (authentication instanceof OAuth2AuthenticationToken) {
                // Fetching OAuth2 client
                OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
                String provider = token.getAuthorizedClientRegistrationId();

                // Fetching OAuth2User Details
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

                if (provider.equalsIgnoreCase(Providers.GOOGLE.toString())) {
                    username = oAuth2User.getAttribute("email");
                } else if (provider.equalsIgnoreCase(Providers.GITHUB.toString())) {
                    String email = oAuth2User.getAttribute("email");
                    username = (email != null) ? email : (oAuth2User.getAttribute("login") + "@gmail.com");
                } else {
                    LOGGER.error("Provider is not available: {}", provider);
                }
            } else {
                // User logged in using form login
                // Fetching UserDetails from authentication
                User user = (User) authentication.getPrincipal();
                username = user.getEmail();
            }

        } catch (Exception e) {
            LOGGER.info("Unexpected error while fetching LoggedInUserEmail: {}", e);
        }
        return username;
    }
}
