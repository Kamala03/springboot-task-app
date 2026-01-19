package org.example.taskproject.mapper;


import org.example.taskproject.dto.UserDtoRequest;
import org.example.taskproject.dto.UserDtoResponse;
import org.example.taskproject.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserEntity toEntity(UserDtoRequest userDtoRequest);
    UserDtoResponse toDto(UserEntity userEntity);
}
