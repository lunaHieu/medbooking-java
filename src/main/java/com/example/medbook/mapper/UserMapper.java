package com.example.medbook.mapper;

import com.example.medbook.dto.request.CreateUserRequest;
import com.example.medbook.dto.request.RegisterRequest;
import com.example.medbook.dto.request.UpdateProfileRequest;
import com.example.medbook.dto.response.UserProfileResponse;
import com.example.medbook.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Chuyển từ RegisterRequest sang User
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    User toUser(RegisterRequest dto);

    // Chuyển từ CreateUserRequest sang User
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    User toUser(CreateUserRequest dto);

    // Entity -> Response (Xem Profile)
    @Mapping(target = "fullName", expression = "java(user.getLastName() != null && user.getFirstName() != null ? (user.getLastName() + \" \" + user.getFirstName()).trim() : \"\")")
    UserProfileResponse toUserProfileResponse(User user);

    //Request -> Entity (Cập nhật Profile)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "firstName", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "lastName", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "dateOfBirth", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "gender", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "address", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "avatarURL", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromRequest(UpdateProfileRequest request, @MappingTarget User user);
}
