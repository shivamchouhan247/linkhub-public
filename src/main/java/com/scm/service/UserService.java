package com.scm.service;

import java.util.Map;

import com.scm.entities.User;
import com.scm.exception.ResourceNotFoundException;
import com.scm.payload.request.RegisterRequest;
import com.scm.payload.request.UpdateProfileRequest;
import com.scm.payload.response.UserInsight;

public interface UserService {
    public Map<String, Object> saveUser(RegisterRequest request);

    public Map<String, Object> getUserById(String userId);

    public User getUserByEmail(String email) throws ResourceNotFoundException;

    public UpdateProfileRequest getUserProfileDetails(String email);

    public Map<String, Object> updateUserProfileDetails(String email, UpdateProfileRequest updateProfileRequest);

    public String resolveProfileImageUrl(String profilePic) throws Exception;

    public UserInsight getUserInsight(String email);
}
