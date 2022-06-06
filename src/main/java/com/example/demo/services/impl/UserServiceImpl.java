package com.example.demo.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.demo.dtos.CredentialsDto;
import com.example.demo.dtos.TweetResponseDto;
import com.example.demo.dtos.UserRequestDto;
import com.example.demo.dtos.UserResponseDto;
import com.example.demo.embeddables.Credentials;
import com.example.demo.embeddables.Profile;
import com.example.demo.entities.Deletable;
import com.example.demo.entities.Tweet;
import com.example.demo.entities.User;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.NotAuthorizedException;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.mappers.TweetMapper;
import com.example.demo.mappers.UserMapper;
import com.example.demo.repositories.TweetRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.UserService;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final TweetRepository tweetRepository;

    private final TweetMapper tweetMapper;

    /*
     * =====================
     * Helper Methods
     * =====================
     */

    private User findUser(String username) {
        Optional<User> optionalUser = userRepository.findByCredentialsUsername(username);
        if (!optionalUser.isPresent() || optionalUser.get().isDeleted()) {
            throw new NotFoundException("User " + username + " not found, are you sure the user is present?");
        }
        return optionalUser.get();
    }

    private <T extends Deletable> List<T> filterDeleted(List<T> toFilter) {
        return toFilter.stream().filter(t -> !t.isDeleted()).collect(Collectors.toList());
    }

    private List<Tweet> reverseChronological(List<Tweet> toSort) {
        List<Tweet> result = new ArrayList<>(toSort);
        result.sort(Comparator.comparing(Tweet::getPosted));
        Collections.reverse(result);
        return result;
    }

    /*
     * =====================
     * Endpoint Methods
     * =====================
     */

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userMapper.entitiesToDtos(userRepository.findAllByDeletedFalse());
    }

    @Override
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        if (userRequestDto == null) {
            throw new BadRequestException("You gotta send me something, at least");
        }
        User userFromRequest = userMapper.dtoToEntity(userRequestDto);

        if (userFromRequest.getProfile() == null || userFromRequest.getProfile().getEmail() == null) {
            throw new BadRequestException("A profile and email are required");
        }
        if (userFromRequest.getCredentials() == null ||
                userFromRequest.getCredentials().getPassword() == null
                || userFromRequest.getCredentials().getUsername() == null) {
            throw new BadRequestException("A username and password are required");
        }

        // can't use findUser here because it would throw an exception
        Optional<User> optionalUser = userRepository
                .findByCredentialsUsername(userRequestDto.getCredentials().getUsername());

        // If a person with this username doesn't exist --> Create an account
        if (!optionalUser.isPresent()) {
            return userMapper.entityToDto(userRepository.saveAndFlush(userFromRequest));
        }

        // A user with that username exist --> do the credentials match?

        if (userFromRequest.getCredentials().getUsername().equals(optionalUser.get().getCredentials().getUsername()) &&
                userFromRequest.getCredentials().getPassword().equals(optionalUser.get().getCredentials().getPassword())
                &&
                optionalUser.get().isDeleted()) {
            optionalUser.get().setDeleted(false);
            for (Tweet t : optionalUser.get().getTweets()) {
                t.setDeleted(false);
            }
            tweetRepository.saveAllAndFlush(optionalUser.get().getTweets());
            return userMapper.entityToDto(userRepository.saveAndFlush(optionalUser.get()));
        }

        // The username is taken and the credentials don't match
        throw new BadRequestException("Username is already taken");

    }

    @Override
    public UserResponseDto getUserByUsername(String username) {
        return userMapper.entityToDto(findUser(username));
    }

    @Override
    public UserResponseDto updateUserProfile(String username, UserRequestDto userRequestDto) {
        if (userRequestDto == null || userRequestDto.getCredentials() == null || userRequestDto.getProfile() == null) {
            throw new BadRequestException("The request or one of its fields was null");
        }
        if (userRequestDto.getCredentials().getPassword() == null ||
                userRequestDto.getCredentials().getUsername() == null) {
            throw new BadRequestException("Username and password are required");
        }

        User toUpdate = findUser(username);

        Credentials toUpdateCredentials = toUpdate.getCredentials();
        if (!toUpdateCredentials.getUsername().equals(userRequestDto.getCredentials().getUsername())
                || !toUpdateCredentials.getPassword().equals(userRequestDto.getCredentials().getPassword())) {
            throw new NotAuthorizedException("Username or password is incorrect");
        }

        Profile updatedProfile = userMapper.dtoToEntity(userRequestDto).getProfile();
        if (updatedProfile.getEmail() != null) {
            toUpdate.getProfile().setEmail(updatedProfile.getEmail());
        }
        if (updatedProfile.getFirstName() != null) {
            toUpdate.getProfile().setFirstName(updatedProfile.getFirstName());
        }
        if (updatedProfile.getLastName() != null) {
            toUpdate.getProfile().setLastName(updatedProfile.getLastName());
        }
        if (updatedProfile.getPhone() != null) {
            toUpdate.getProfile().setPhone(updatedProfile.getPhone());
        }

        return userMapper.entityToDto(userRepository.saveAndFlush(toUpdate));
    }

    @Override
    public UserResponseDto deleteUser(String username, CredentialsDto credentialsDto) {
        User toDelete = findUser(username);

        if (!toDelete.getCredentials().getUsername().equals(credentialsDto.getUsername()) ||
                !toDelete.getCredentials().getPassword().equals(credentialsDto.getPassword())) {
            throw new NotAuthorizedException("Incorrect username or password");
        }

        toDelete.setDeleted(true);

        for (Tweet t : toDelete.getTweets()) {
            t.setDeleted(true);
        }
        tweetRepository.saveAll(toDelete.getTweets()); // saving outside the loop reduces calls to DB improving
                                                       // performance

        return userMapper.entityToDto(userRepository.saveAndFlush(toDelete));
    }

    @Override
    public void followUser(String username, CredentialsDto credentialsDto) {
        User userToFollow = findUser(username);
        User followingUser = findUser(credentialsDto.getUsername());

        if (!followingUser.getCredentials().getPassword().equals(credentialsDto.getPassword())) {
            throw new NotAuthorizedException("Username or password are incorrect");
        }

        if (followingUser.getFollowing().contains(userToFollow)) {
            throw new BadRequestException("You already follow the user provided to unfollow");
        }

        followingUser.getFollowing().add(userToFollow);
        userRepository.saveAndFlush(followingUser);
    }

    @Override
    public void unfollowUser(String username, CredentialsDto credentialsDto) {
        User userToUnFollow = findUser(username);
        User followingUser = findUser(credentialsDto.getUsername());

        if (!followingUser.getCredentials().getPassword().equals(credentialsDto.getPassword())) {
            throw new NotAuthorizedException("Username or password are incorrect");
        }

        if (!followingUser.getFollowing().contains(userToUnFollow)) {
            throw new BadRequestException("You don't follow the user provided to unfollow");
        }

        followingUser.getFollowing().remove(userToUnFollow);
        userRepository.saveAndFlush(followingUser);
    }

    @Override
    public List<TweetResponseDto> getUserFeed(String username) {
        User user = findUser(username);

        List<Tweet> feed = user.getTweets();
        for (User followed : user.getFollowing()) {
            feed.addAll(followed.getTweets());
        }

        return tweetMapper.entitiesToDtos(reverseChronological(filterDeleted(feed)));
    }

    @Override
    public List<TweetResponseDto> getUserTweets(String username) {
        return tweetMapper.entitiesToDtos(reverseChronological(filterDeleted(findUser(username).getTweets())));
    }

    @Override
    public List<TweetResponseDto> getUserMentions(String username) {
        return tweetMapper
                .entitiesToDtos(reverseChronological(filterDeleted(findUser(username).getMentioningTweets())));
    }

    @Override
    public List<UserResponseDto> getUserFollowers(String username) {
        return userMapper.entitiesToDtos(filterDeleted(findUser(username).getFollowers()));
    }

    @Override
    public List<UserResponseDto> getUserFollowing(String username) {
        return userMapper.entitiesToDtos(filterDeleted(findUser(username).getFollowing()));
    }

}
