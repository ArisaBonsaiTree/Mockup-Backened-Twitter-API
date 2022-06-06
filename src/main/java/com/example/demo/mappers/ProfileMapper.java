package com.example.demo.mappers;

import com.example.demo.dtos.ProfileDto;
import com.example.demo.embeddables.Profile;
import org.mapstruct.Mapper;

import com.example.demo.dtos.UserResponseDto;
import com.example.demo.entities.User;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileDto embeddableToDto(Profile profile);

    Profile dtoToEmbeddable(ProfileDto profileDto);

}
