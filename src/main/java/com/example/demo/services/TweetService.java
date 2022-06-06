package com.example.demo.services;

import java.util.List;

import com.example.demo.dtos.ContextResponseDto;
import com.example.demo.dtos.CredentialsDto;
import com.example.demo.dtos.HashtagResponseDto;
import com.example.demo.dtos.TweetRequestDto;
import com.example.demo.dtos.TweetResponseDto;
import com.example.demo.dtos.UserResponseDto;

public interface TweetService {

    List<TweetResponseDto> getAllTweets();

    TweetResponseDto createTweet(TweetRequestDto tweetRequestDto);

    TweetResponseDto getTweetById(Long id);

    TweetResponseDto deleteTweet(Long id, CredentialsDto credentialsDto);

    void likeTweet(Long id, CredentialsDto credentialsDto);

    TweetResponseDto createReplyTweet(Long id, TweetRequestDto tweetRequestDto);

    TweetResponseDto createRepostTweet(Long id, CredentialsDto credentialsDto);

    List<HashtagResponseDto> getTweetTags(Long id);

    List<UserResponseDto> getTweetLikes(Long id);

    ContextResponseDto getTweetContext(Long id);

    List<TweetResponseDto> getTweetReplies(Long id);

    List<TweetResponseDto> getTweetReposts(Long id);

    List<UserResponseDto> getTweetMentions(Long id);
}
