package com.example.demo.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.demo.dtos.HashtagResponseDto;
import com.example.demo.dtos.TweetResponseDto;
import com.example.demo.entities.Hashtag;
import com.example.demo.entities.Tweet;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.mappers.HashtagMapper;
import com.example.demo.mappers.TweetMapper;
import com.example.demo.repositories.HashtagRepository;
import com.example.demo.services.HashtagService;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class HashtagServiceImpl implements HashtagService {

    private final TweetMapper tweetMapper;

    private final HashtagRepository hashtagRepository;

    private final HashtagMapper hashtagMapper;

    @Override
    public List<HashtagResponseDto> getAllHashtags() {
        return hashtagMapper.entitiesToDtos(hashtagRepository.findAll());
    }

    @Override
    public List<TweetResponseDto> getTweetsByTag(String label) {
        Optional<Hashtag> optionalHashtag = hashtagRepository.findByLabel(label);
        if (!optionalHashtag.isPresent()) {
            throw new NotFoundException("Hashtag does not exist");
        }
        List<Tweet> tweetsWithTag = new ArrayList<>();
        for (Tweet tweet : optionalHashtag.get().getTweets()) {
            if (!tweet.isDeleted()) {
                tweetsWithTag.add(tweet);
            }
        }
        return tweetMapper.entitiesToDtos(tweetsWithTag);
    }

}
