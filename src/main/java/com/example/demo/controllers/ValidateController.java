package com.example.demo.controllers;

import com.example.demo.services.ValidateService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("validate")
@AllArgsConstructor
public class ValidateController {

    private final ValidateService validateService;

    @GetMapping("tag/exists/{label}")
    public boolean tagExists(@PathVariable String label) {
        return validateService.tagExists(label);
    }

    @GetMapping("username/exists/@{username}")
    public boolean usernameExists(@PathVariable String username) {
        return validateService.usernameExists(username);
    }

    @GetMapping("username/available/@{username}")
    public boolean isUsernameAvailable(@PathVariable String username) {
        return validateService.isUsernameAvailable(username);
    }

}
