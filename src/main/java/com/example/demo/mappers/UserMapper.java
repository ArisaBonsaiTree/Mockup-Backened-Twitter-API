package com.example.demo.mappers;

import com.example.demo.dtos.UserRequestDto;
import com.example.demo.dtos.UserResponseDto;
import com.example.demo.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapProperties.Credential;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProfileMapper.class, CredentialsMapper.class})
public interface UserMapper {
    @Mapping(target = "username", source = "credentials.username")
    UserResponseDto entityToDto(User entity);

    List<UserResponseDto> entitiesToDtos(List<User> entities);

    User createDtoToEntity(UserResponseDto userRequestDto);

    User dtoToEntity(UserRequestDto userRequestDto);

    static String credentialToUsername(Credential credential) {
        return credential.getUsername();
    }


}


