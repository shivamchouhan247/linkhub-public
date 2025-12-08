package com.scm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.scm.entities.User;
import com.scm.payload.request.UpdateProfileRequest;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "imageFile",ignore = true)
    UpdateProfileRequest toUpdateProfileRequest(User user);
}
