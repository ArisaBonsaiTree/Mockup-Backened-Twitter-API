package com.example.demo.services.impl;

import com.example.demo.repositories.HashtagRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.ValidateService;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidateServiceImpl implements ValidateService {

    private final UserRepository userRepository;

    private final HashtagRepository hashtagRepository;

    @Override
    public boolean tagExists(String label) {
        return hashtagRepository.findByLabel(label).isPresent();
    }

    @Override
    public boolean usernameExists(String username) {
        return userRepository.findByCredentialsUsername(username).isPresent();
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.findByCredentialsUsername(username).isPresent();
    }

}
