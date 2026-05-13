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

@Mapper(componentModel = "spring") // Báo cho Spring và MapStruct biết
public interface UserMapper {

    // Chuyển từ RegisterRequest sang User
    @Mapping(target = "userId", ignore = true) // Bỏ qua khi tạo mới
    @Mapping(target = "role", ignore = true) // Sẽ được set thủ công
    @Mapping(target = "status", ignore = true) // Sẽ được set thủ công
    @Mapping(target = "passwordHash", ignore = true) // Sẽ được mã hóa & set thủ công
    User toUser(RegisterRequest dto);

    // Chuyển từ CreateUserRequest sang User
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    // 'role' có trong DTO này, nên ta không 'ignore' nó
    User toUser(CreateUserRequest dto);
    // Entity -> Response (Xem Profile)
    UserProfileResponse toUserProfileResponse(User user);

    //Request -> Entity (Cập nhật Profile)
    // Chỉ cập nhật các trường không null (người dùng không gửi thì giữ nguyên cái cũ)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "email", ignore = true) // Không cho đổi email ở đây
    @Mapping(target = "phoneNumber", ignore = true) // Không cho đổi SĐT ở đây
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
