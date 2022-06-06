package com.example.demo.services.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

import com.example.demo.dtos.ContextResponseDto;
import com.example.demo.dtos.CredentialsDto;
import com.example.demo.dtos.HashtagResponseDto;
import com.example.demo.dtos.TweetRequestDto;
import com.example.demo.dtos.TweetResponseDto;
import com.example.demo.dtos.UserResponseDto;
import com.example.demo.entities.Deletable;
import com.example.demo.entities.Hashtag;
import com.example.demo.entities.Tweet;
import com.example.demo.entities.User;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.NotAuthorizedException;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.mappers.HashtagMapper;
import com.example.demo.mappers.TweetMapper;
import com.example.demo.mappers.UserMapper;
import com.example.demo.repositories.HashtagRepository;
import com.example.demo.repositories.TweetRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.TweetService;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TweetServiceImpl implements TweetService {

    private TweetRepository tweetRepository;
    private TweetMapper tweetMapper;
    private UserRepository userRepository;
    private UserMapper userMapper;
    private HashtagRepository hashtagRepository;
    private HashtagMapper hashtagMapper;

    /*
     * =====================
     * Helper Methods
     * =====================
     */
    private User getUser(String username, String password) {
        Optional<User> optionalUser = userRepository.findByCredentialsUsername(username);

        // Checks to see if the user exist
        if (optionalUser.isEmpty() || optionalUser.get().isDeleted()) {
            throw new NotFoundException("This user doesn't exist: " + username);
        }

        // Checks to see if the user is authorized to make this tweet
        if (!optionalUser.get().getCredentials().getPassword().equals(password)) {
            throw new NotAuthorizedException("Username or password isn't correct");
        }

        return optionalUser.get();
    }

    private Tweet getTweet(Long id) {
        // Not only finds a Tweet by id, but will also prevent a person from seeing a
        // Tweet marked for deletion
        Optional<Tweet> optionalTweet = tweetRepository.findByIdAndDeletedFalse(id);

        if (optionalTweet.isEmpty() || optionalTweet.get().isDeleted()) {
            throw new NotFoundException("No tweet with this id found: " + id);
        }

        return optionalTweet.get();
    }

    private <T extends Deletable> List<T> filterDeleted(Collection<T> toFilter) {
        return toFilter.stream().filter(t -> !t.isDeleted()).collect(Collectors.toList());
    }

    private List<Tweet> getTweetsBefore(Tweet tweet) {
        List<Tweet> tweetList = new ArrayList<>();
        Tweet actualTweet = tweet;
        while (actualTweet.getInReplyTo() != null) {
            actualTweet = actualTweet.getInReplyTo();
            if (!actualTweet.isDeleted())
                tweetList.add(actualTweet);
        }
        return tweetList;
    }

    private List<Tweet> getTweetsAfter(Tweet tweet) {
        List<Tweet> tweetList = new ArrayList<>();
        Queue<Tweet> tweetQueue = new LinkedList<>();
        tweetQueue.addAll(tweet.getReplies());

        while (!tweetQueue.isEmpty()) {
            Tweet actualTweet = tweetQueue.poll();
            if (!actualTweet.isDeleted()) {
                tweetList.add(actualTweet);
            }
            tweetQueue.addAll(actualTweet.getReplies());
        }
        return tweetList;
    }

    private boolean userCredsDontMatch(TweetRequestDto tweetRequestDto) {
        Optional<User> user = userRepository.findByCredentialsUsername(tweetRequestDto.getCredentials().getUsername());
        if (user.isEmpty() || user.get().isDeleted()) {
            throw new NotAuthorizedException("User doesn't exist");
        }
        return !user.get().getCredentials().getUsername().equals(tweetRequestDto.getCredentials().getUsername()) ||
                !user.get().getCredentials().getPassword().equals(tweetRequestDto.getCredentials().getPassword());
    }

    private List<Tweet> reverseChronological(List<Tweet> toSort) {
        List<Tweet> result = new ArrayList<>(toSort);
        result.sort(Comparator.comparing(Tweet::getPosted));
        Collections.reverse(result);
        return result;
    }

    private void parseHashtags(Tweet tweetToSave) {
        // HashTags
        // Proof of concept > Optimization
        String[] contentBreak = tweetToSave.getContent().split("\\s+");
        for (String s : contentBreak) {
            if (s.startsWith("#")) {
                s = s.substring(1);

                // Hashtag h = hashtagRepository.findByLabel(s).get();
                Optional<Hashtag> h = hashtagRepository.findByLabel(s);

                // If the tag already exist
                if (h.isPresent()) {

                    // Update last used in the database
                    h.get().setLastUsed(Timestamp.valueOf(LocalDateTime.now()));
                    h.get().getTweets().add(tweetToSave);

                    hashtagRepository.save(h.get());

                    // Add the Hashtag to the tweet arrayList
                    tweetToSave.getHashtags().add(h.get());
                }

                // The HashTah does not exist --> we need to create it
                else {
                    // Add the HashTag to the database
                    Hashtag newHashtag = new Hashtag();
                    newHashtag.setLabel(s);
                    newHashtag.getTweets().add(tweetToSave);
                    newHashtag.setLastUsed(Timestamp.valueOf(LocalDateTime.now()));
                    newHashtag.setFirstUsed(Timestamp.valueOf(LocalDateTime.now()));

                    hashtagRepository.saveAndFlush(newHashtag);
                    tweetToSave.getHashtags().add(newHashtag);
                }
            }
        }
    }

    // TODO: FIX
    private void parseMentions(Tweet tweetToSave) {
        String[] contentBreak = tweetToSave.getContent().split("\\s+");

        for (String s : contentBreak) {

            if (s.startsWith("@")) {

                s = s.substring(1);

                Optional<User> h = userRepository.findByCredentialsUsername(s);

                if (h.isPresent()) {
                    // Add the Tweet to the user
                    h.get().getMentioningTweets().add(tweetToSave);

                    // Link the User to the tweet
                    tweetToSave.getMentionedUsers().add(h.get());

                    userRepository.save(h.get());
                    tweetRepository.save(tweetToSave);
                }
            }
        }

        // End of for-loop

    }

    /*
     * =====================
     * Endpoint Methods
     * =====================
     */
    @Override
    public List<TweetResponseDto> getAllTweets() {
        return tweetMapper.entitiesToDtos(reverseChronological(tweetRepository.findAllByDeletedFalse()));
    }

    @Override
    public TweetResponseDto createTweet(TweetRequestDto tweetRequestDto) {
        if (tweetRequestDto.getCredentials() == null) {
            throw new NotAuthorizedException("Missing credentials");
        }

        if (tweetRequestDto.getContent() == null) {
            throw new NotFoundException("Missing content");
        }

        if (userCredsDontMatch(tweetRequestDto)) {
            throw new NotAuthorizedException("Incorrect username or password");
        }

        Tweet tweetToSave = tweetMapper.requestDtoToEntity(tweetRequestDto);

        tweetToSave.setAuthor(getUser(tweetRequestDto.getCredentials().getUsername(),
                tweetRequestDto.getCredentials().getPassword()));

        tweetRepository.saveAndFlush(tweetToSave); // save before parsing so that tweet exists in DB in case adding to
                                                   // user or tag

        parseMentions(tweetToSave);
        parseHashtags(tweetToSave);

        return tweetMapper.entityToDto(tweetRepository.saveAndFlush(tweetToSave));
    }

    @Override
    public TweetResponseDto getTweetById(Long id) {
        return tweetMapper.entityToDto(getTweet(id));
    }

    @Override
    public TweetResponseDto deleteTweet(Long id, CredentialsDto credentialsDto) {

        // Checks to see if the user even sent any credentials
        if (credentialsDto == null) {
            throw new BadRequestException("Missing credentials");
        }

        if (credentialsDto.getUsername() == null || credentialsDto.getPassword() == null) {
            throw new BadRequestException("Missing username or password");
        }

        // Checks to see if the tweet even exist
        Tweet tweetToDelete = getTweet(id);

        // Check to see if the user is authorized to 'delete' the tweet
        if (!(credentialsDto.getUsername().equals(tweetToDelete.getAuthor().getCredentials().getUsername()))
                || !(credentialsDto.getPassword().equals(tweetToDelete.getAuthor().getCredentials().getPassword()))) {
            throw new NotAuthorizedException("Not the author of the tweet");
        }

        // Mark the Tweet as deleted to prevent it from appearing, save it to the
        // database, and display it
        tweetToDelete.setDeleted(true);
        return tweetMapper.entityToDto(tweetRepository.saveAndFlush(tweetToDelete));
    }

    @Override
    public void likeTweet(Long id, CredentialsDto credentialsDto) {
        Tweet tweetToLike = getTweet(id);
        User userThatLikedTweet = getUser(credentialsDto.getUsername(), credentialsDto.getPassword());

        // Add the user to the liking users list
        if (!tweetToLike.getLikingUsers().contains(userThatLikedTweet)) {
            tweetToLike.getLikingUsers().add(userThatLikedTweet);
            tweetRepository.saveAndFlush(tweetToLike);
        }
    }

    @Override
    public TweetResponseDto createReplyTweet(Long id, TweetRequestDto tweetRequestDto) {
        if (tweetRequestDto.getCredentials() == null) {
            throw new NotAuthorizedException("Missing credentials");
        }

        if (tweetRequestDto.getContent() == null) {
            throw new NotFoundException("Missing content");
        }

        if (userCredsDontMatch(tweetRequestDto)) {
            throw new NotAuthorizedException("Incorrect username or password");
        }

        Tweet tweetToSave = tweetMapper.requestDtoToEntity(tweetRequestDto);

        tweetToSave.setAuthor(getUser(tweetRequestDto.getCredentials().getUsername(),
                tweetRequestDto.getCredentials().getPassword()));

        tweetToSave.setInReplyTo(getTweet(id));

        tweetRepository.saveAndFlush(tweetToSave); // save before parsing so that tweet exists in DB in case adding to
                                                   // user or tag

        parseMentions(tweetToSave);
        parseHashtags(tweetToSave);

        return tweetMapper.entityToDto(tweetRepository.saveAndFlush(tweetToSave));
    }

    @Override
    public TweetResponseDto createRepostTweet(Long id, CredentialsDto credentialsDto) {
        Tweet getTweet = getTweet(id);
        User getUser = getUser(credentialsDto.getUsername(), credentialsDto.getPassword());

        Tweet repostTweet = new Tweet();

        repostTweet.setAuthor(getUser);
        repostTweet.setRepostOf(getTweet);

        repostTweet.setContent(null);
        repostTweet.setInReplyTo(null);

        tweetRepository.saveAndFlush(repostTweet);

        return tweetMapper.entityToDto(repostTweet);
    }

    @Override
    public List<HashtagResponseDto> getTweetTags(Long id) {
        return hashtagMapper.entitiesToDtos(getTweet(id).getHashtags());
    }

    @Override
    public List<UserResponseDto> getTweetLikes(Long id) {
        return userMapper.entitiesToDtos(filterDeleted(getTweet(id).getLikingUsers()));
    }

    @Override
    public ContextResponseDto getTweetContext(Long id) {
        Tweet tweet = getTweet(id);
        ContextResponseDto responseDto = new ContextResponseDto();
        responseDto.setTarget(tweetMapper.entityToDto(tweet));
        responseDto.setBefore(tweetMapper.entitiesToDtos(getTweetsBefore(tweet)));
        responseDto.setAfter(tweetMapper.entitiesToDtos(getTweetsAfter(tweet)));
        return responseDto;
    }

    @Override
    public List<TweetResponseDto> getTweetReplies(Long id) {
        return tweetMapper.entitiesToDtos(filterDeleted(getTweet(id).getReplies()));
    }

    @Override
    public List<TweetResponseDto> getTweetReposts(Long id) {
        return tweetMapper.entitiesToDtos(filterDeleted(getTweet(id).getReposts()));
    }

    @Override
    public List<UserResponseDto> getTweetMentions(Long id) {
        return userMapper.entitiesToDtos(filterDeleted(getTweet(id).getMentionedUsers()));
    }
}