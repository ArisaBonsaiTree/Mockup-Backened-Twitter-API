package com.example.demo.mappers;

import com.example.demo.dtos.HashtagResponseDto;
import com.example.demo.entities.Hashtag;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HashtagMapper {

    HashtagResponseDto entityToDto(Hashtag entity);

    List<HashtagResponseDto> entitiesToDtos(List<Hashtag> entities);

}
