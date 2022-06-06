package com.example.demo.services;

import com.example.demo.dtos.HashtagResponseDto;
import com.example.demo.dtos.TweetResponseDto;

import java.util.List;

public interface HashtagService {

    List<HashtagResponseDto> getAllHashtags();

    List<TweetResponseDto> getTweetsByTag(String label);

}
