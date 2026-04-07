package com.orderapp.user_service.mapper;


import com.orderapp.user_service.dto.CreateUserRequest;
import com.orderapp.user_service.dto.UserResponse;
import com.orderapp.user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "password", target = "passwordHash")
    User toEntity(CreateUserRequest request);

    UserResponse toResponse(User user);
}