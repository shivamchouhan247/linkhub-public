package com.scm.controllers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.scm.enums.MessageAlertType;
import com.scm.payload.request.UpdateProfileRequest;
import com.scm.payload.response.UserInsight;
import com.scm.service.UserService;
import com.scm.utils.constants.ResponseMessage;
import com.scm.utils.helper.AlertMessage;
import com.scm.utils.helper.UserHelper;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/user")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    // user dashboard page controller
    @RequestMapping(value = "/dashboard")
    public String userDashboard(Model model, Authentication authentication) {
        UserInsight userInshight = new UserInsight();
        try {
            String email = UserHelper.getLoggedInUserEmail(authentication);
            userInshight = userService.getUserInsight(email);
        } catch (Exception e) {

        }
        model.addAttribute("userInshight", userInshight);
        return "user/dashboard";
    }

    // user profile page controller
    @RequestMapping(value = "/profile")
    public String getProfileDetails(Model model, Authentication authentication) {
        LOGGER.info("inside profile:");
        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest();
        try {
            String email = UserHelper.getLoggedInUserEmail(authentication);
            updateProfileRequest = userService.getUserProfileDetails(email);
            LOGGER.info("Profile Details: {}", updateProfileRequest);
        } catch (Exception e) {

        }
        model.addAttribute("updateProfileRequest", updateProfileRequest);
        return "user/profile";
    }

    @RequestMapping(value = "/profile/update", method = RequestMethod.POST)
    public String updateContactDetails(
            @Valid @ModelAttribute UpdateProfileRequest updateProfileRequest,
            BindingResult bindingResult, Authentication authentication, HttpSession session) {
        LOGGER.info("Update user profile request: {}", updateProfileRequest.getEmail());
        AlertMessage message = null;
        try {
            if (bindingResult.hasErrors()) {
                LOGGER.warn("Profile update validation errors: {}", bindingResult);
                return "/user/profile";
            }

            // update the user profile details in db
            String email = UserHelper.getLoggedInUserEmail(authentication);
            Map<String, Object> response = userService.updateUserProfileDetails(email, updateProfileRequest);
            MessageAlertType type = MessageAlertType.fromCode(response.get(ResponseMessage.CODE).toString());
            message = AlertMessage.builder()
                    .type(type)
                    .content(response.get(ResponseMessage.DESCRIPTION).toString())
                    .build();

            LOGGER.info("updateRequest: {}", response);
        } catch (Exception e) {
            LOGGER.error("Unexpected error during updating user profile details: {},{}",
                    updateProfileRequest.getEmail(), e);
            message = AlertMessage.builder()
                    .type(MessageAlertType.red)
                    .content("Unable to update profile at this time.")
                    .build();
        }

        session.setAttribute("message", message);
        return "redirect:/user/profile";
    }


}
