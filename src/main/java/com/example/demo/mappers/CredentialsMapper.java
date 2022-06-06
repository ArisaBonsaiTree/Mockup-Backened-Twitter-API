package com.example.demo.mappers;

import com.example.demo.dtos.CredentialsDto;
import com.example.demo.embeddables.Credentials;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CredentialsMapper {

    Credentials dtoToEmbeddable(CredentialsDto credentialsDto);

}
