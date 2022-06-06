package com.example.demo.services;

import java.util.List;

import com.example.demo.dtos.CredentialsDto;
import com.example.demo.dtos.TweetResponseDto;
import com.example.demo.dtos.UserRequestDto;
import com.example.demo.dtos.UserResponseDto;

public interface UserService {

    List<UserResponseDto> getAllUsers();

    UserResponseDto createUser(UserRequestDto userRequestDto);

    UserResponseDto getUserByUsername(String username);

    UserResponseDto updateUserProfile(String username, UserRequestDto userRequestDto);

    UserResponseDto deleteUser(String username, CredentialsDto credentialsDto);

    void followUser(String username, CredentialsDto credentialsDto);

    void unfollowUser(String username, CredentialsDto credentialsDto);

    List<TweetResponseDto> getUserFeed(String username);

    List<TweetResponseDto> getUserTweets(String username);

    List<TweetResponseDto> getUserMentions(String username);

    List<UserResponseDto> getUserFollowers(String username);

    List<UserResponseDto> getUserFollowing(String username);

}
