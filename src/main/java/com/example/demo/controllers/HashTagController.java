package com.example.demo.controllers;

import com.example.demo.dtos.HashtagResponseDto;
import com.example.demo.dtos.TweetResponseDto;
import com.example.demo.services.HashtagService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("tags")
@AllArgsConstructor
public class HashTagController {

    private final HashtagService hashtagService;

    @GetMapping
    public List<HashtagResponseDto> getAllHashtags() {
        return hashtagService.getAllHashtags();
    }

    @GetMapping("/{label}")
    public List<TweetResponseDto> getTweetsByTag(@PathVariable String label) {
        return hashtagService.getTweetsByTag(label);
    }

}
