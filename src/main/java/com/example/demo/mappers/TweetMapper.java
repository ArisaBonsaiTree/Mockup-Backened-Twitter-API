package com.example.demo.mappers;

import com.example.demo.dtos.TweetRequestDto;
import com.example.demo.dtos.TweetResponseDto;
import com.example.demo.entities.Tweet;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface TweetMapper {

    TweetResponseDto entityToDto(Tweet entity);

    List<TweetResponseDto> entitiesToDtos(List<Tweet> entities);

    Tweet requestDtoToEntity(TweetRequestDto tweetRequestDto);
}
