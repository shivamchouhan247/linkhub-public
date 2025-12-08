package com.scm.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.scm.entities.Contact;
import com.scm.entities.User;
import com.scm.exception.ResourceNotFoundException;
import com.scm.mapper.UserMapper;
import com.scm.payload.request.RegisterRequest;
import com.scm.payload.request.UpdateProfileRequest;
import com.scm.payload.response.UserInsight;
import com.scm.repo.UserRepository;
import com.scm.service.ContactService;
import com.scm.service.S3Service;
import com.scm.service.UserService;
import com.scm.utils.constants.CommonConfigLogic;
import com.scm.utils.constants.ResponseMessage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    @Lazy
    private ContactService contactService;
    @Autowired
    private CommonConfigLogic commonConfigLogic;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private UserMapper userMapper;

    @Value("${default.user.profilepic}")
    private String defaultProfileUrl;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public Map<String, Object> saveUser(RegisterRequest request) {
        Map<String, Object> map = new HashMap<>();
        LOGGER.info("Inside saveUser:");
        try {

            String email = request.getEmail();
            boolean isExists = userRepository.existsByEmail(email);
            if (isExists) {
                return commonConfigLogic.buildResponse(ResponseMessage.CONFLICT, ResponseMessage.STATUS_FAILED,
                        "User is already registered with provided email");
            }

            String userId = UUID.randomUUID().toString();
            LOGGER.info("userId: {}", userId);

            User user = User.builder()
                    .userId(userId)
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .about(request.getAbout())
                    .enabled(true)
                    .roles(List.of(ResponseMessage.ROLE_USER))
                    .build();

            User savedUser = userRepository.save(user);
            map = commonConfigLogic.buildResponse(ResponseMessage.SUCCESS, ResponseMessage.STATUS_SUCCESS,
                    ResponseMessage.REGISTERED_SUCCESSFULLY);
            map.put("savedUser", savedUser);

        } catch (Exception e) {
            LOGGER.error("Unexpected error during user registration with email: {}", request.getEmail(), e);
            return commonConfigLogic.buildResponse(ResponseMessage.FAILED, ResponseMessage.STATUS_FAILED,
                    ResponseMessage.DESC_SOMETHING_WENT_WRONG);

        }

        return map;
    }

    @Override
    public Map<String, Object> getUserById(String userId) {
        LOGGER.info("Fetching user by ID: {}", userId);
        Map<String, Object> response;
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("User not found with the given user id: " + userId));
            response = commonConfigLogic.buildResponse(ResponseMessage.SUCCESS, ResponseMessage.STATUS_SUCCESS,
                    "User found successfully");
            response.put("user", user);

        } catch (ResourceNotFoundException e) {
            LOGGER.warn("User not found with ID: {}", userId, e);
            return commonConfigLogic.buildResponse(ResponseMessage.NOT_FOUND, ResponseMessage.STATUS_FAILED,
                    e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected Error while fetching user by ID: {}", userId);
            return commonConfigLogic.buildResponse(ResponseMessage.FAILED, ResponseMessage.STATUS_FAILED,
                    "Something went wrong");
        }
        return response;
    }

    @Override
    public User getUserByEmail(String email) throws ResourceNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with the given user email: " + email));
    }

    @Override
    public String resolveProfileImageUrl(String profilePic) throws Exception {
        if (profilePic == null || profilePic.isEmpty()) {
            return defaultProfileUrl;
        } else if (profilePic.startsWith("http")) {
            return profilePic;
        }

        return s3Service.generatePresignedUrl(profilePic);

    }

    @Override
    public UpdateProfileRequest getUserProfileDetails(String email) {
        UpdateProfileRequest userProfileDetails = new UpdateProfileRequest();
        try {
            User user = getUserByEmail(email);
            userProfileDetails = userMapper.toUpdateProfileRequest(user);

            // Handling Profile Url -> Private S3 bucket
            userProfileDetails.setProfilePic(resolveProfileImageUrl(userProfileDetails.getProfilePic()));

        } catch (Exception e) {
            LOGGER.info("Unexpected error during getting user profile details: {}, {}",
                    email, e);
        }
        return userProfileDetails;
    }

    @Override
    public Map<String, Object> updateUserProfileDetails(String email, UpdateProfileRequest updateProfileRequest) {
        Map<String, Object> map = new HashMap<>();
        LOGGER.info("Inside updateUserProfileDetails:");
        try {

            User user = getUserByEmail(email);

            if (!user.getEmail().equals(updateProfileRequest.getEmail())) {
                return commonConfigLogic.buildResponse(ResponseMessage.FAILED, ResponseMessage.STATUS_FAILED,
                        "Email cannot be changed from this form.");
            }

            // updating user
            user.setName(updateProfileRequest.getName().trim());
            user.setPhoneNumber(updateProfileRequest.getPhoneNumber().trim());
            user.setAbout(updateProfileRequest.getAbout().trim());

            // processing image profile
            MultipartFile imageFile = updateProfileRequest.getImageFile();
            if (imageFile != null && !imageFile.isEmpty()) {
                String profilePic = s3Service.uploadFile(imageFile);
                user.setProfilePic(profilePic);
                LOGGER.info("profile is Successfully uploaded on S3 with key: {}", profilePic);
            }

            User savedUser = userRepository.save(user);
            map = commonConfigLogic.buildResponse(ResponseMessage.SUCCESS, ResponseMessage.STATUS_SUCCESS,
                    "User Profile Details Updated Successfully.");
            map.put("savedUser", savedUser);

        } catch (Exception e) {
            LOGGER.error("Unexpected error during update user profile details: {}", email, e);
            return commonConfigLogic.buildResponse(ResponseMessage.FAILED, ResponseMessage.STATUS_FAILED,
                    ResponseMessage.DESC_SOMETHING_WENT_WRONG);
        }

        return map;
    }

    @Override
    public UserInsight getUserInsight(String email) {
        UserInsight userInsight = new UserInsight();
        try {
            User user = getUserByEmail(email);
            List<Contact> contacts = contactService.getUserSavedContacts(user.getUserId());
            long totalFavorite = contacts.stream().filter(Contact::isFavorite).count();
            int totalSocialLinks = contacts.stream().mapToInt(c -> c.getSocialLinks().size()).sum();

            long thirtyDayAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
            long monthlyContacts = contacts.stream()
                    .filter(c -> c.getCreationDate().getTime() >= thirtyDayAgo)
                    .count();

            userInsight = UserInsight.builder()
                    .totalContact(contacts.size())
                    .favoriteCount((int) totalFavorite)
                    .socialLinkCount(totalSocialLinks)
                    .monthlyContacts((int) monthlyContacts)
                    .build();
        } catch (Exception e) {
            LOGGER.error("Error while calculating user insight for email: {}", email, e.getMessage());

        }
        return userInsight;
    }

}
