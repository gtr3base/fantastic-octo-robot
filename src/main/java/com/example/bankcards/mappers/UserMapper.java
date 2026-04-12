package com.example.bankcards.mappers;

import com.example.bankcards.dto.requests.RegisterRequest;
import com.example.bankcards.dto.responses.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.enums.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, imports = UserRole.class,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
public interface UserMapper {
    @Mapping(target = "role", expression = "java(UserRole.USER)")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toUser(RegisterRequest req);

    UserResponse toResponse(User user);
}
