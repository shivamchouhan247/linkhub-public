package com.scm.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.scm.entities.User;
import com.scm.service.UserService;
import com.scm.utils.helper.UserHelper;

@ControllerAdvice
public class RootController {

    private final UserService userService;

    public RootController(UserService userService) {
        this.userService = userService;
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(RootController.class);

    @ModelAttribute
    public void addLoggedInUserInfo(Model model, Authentication authentication) {
        try {
            if (authentication == null) {
                return;// if user is not logged in do not add userInfo in model
            }
            // LOGGER.info("Adding userInfo in the request.");
            // Fetch username from authentication
            String username = UserHelper.getLoggedInUserEmail(authentication);

            User user = userService.getUserByEmail(username);
            user.setProfilePic(userService.resolveProfileImageUrl(user.getProfilePic()));

            // Adding userInfo in model for every request
            model.addAttribute("loggedInUser", user);

            // LOGGER.info("LoggedInUser: {}",
            // model.getAttribute("loggedInUser").toString());

        } catch (Exception e) {
            LOGGER.info("Unexpected error while adding LoggedIn UserInfo: {}", e);
        }
    }
}
