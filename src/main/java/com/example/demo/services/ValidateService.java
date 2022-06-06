package com.example.demo.services;

public interface ValidateService {

    boolean tagExists(String label);

    boolean usernameExists(String username);

    boolean isUsernameAvailable(String username);

}
