package com.example.demo.repositories;

import com.example.demo.entities.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, Long> {

    // Adding this allows us to view Tweets that are not marked as deletion
    List<Tweet> findAllByDeletedFalse();

    // Prevent getId(Long id) from throwing its own exception
    Optional<Tweet> findById(Long id);

    // getById and check to see if the deleted tag is false --> NOT MARKED FOR DELETION
    Optional<Tweet> findByIdAndDeletedFalse(Long id);

    List<Tweet> findByRepostOfIsNotNull();

    List<Tweet> findByRepostOfIsNotNullAndId(Long id);

    List<Tweet> findByRepostOfIsNotNullAndRepostOfId(Long id);

    List<Tweet> findByRepostOfIsNotNullAndRepostOfIdAndRepostOfDeletedFalse(Long id);

    List<Tweet> findByDeletedFalseAndAuthorId(Long id);

    List<Tweet> findByAuthorIdAndDeletedTrue(Long id);
}

//Optional<User> userOptional = userRepository.findByCredentialsUsername(username);
//        if (userOptional.isEmpty())
//            return null;
//        return userOptional.get();
