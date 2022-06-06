package com.example.demo.entities;

import com.example.demo.embeddables.Credentials;
import com.example.demo.embeddables.Profile;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_table")
@NoArgsConstructor
@Data
public class User implements Deletable {

    @Id
    @GeneratedValue
    private Long id;

    // the attribute overrides you could accomplish in the embeddable classes.
    // I'm moving what you're overriding in to those for now.
    @Embedded
    private Credentials credentials;

    // Initializing the Timestamp on Object creation here means you don't have to do
    // it in the service.
    // This will also automatically be overridden when JPA pulls from the DB because
    // it uses Getters and Setters
    // to update the fields.
    @CreationTimestamp
    private Timestamp joined = Timestamp.valueOf(LocalDateTime.now());

    // Use @Column(nullable = false) to define where you need not null in the DB
    // Also initializing here is the same thing as with the timestamp above. All
    // objects upon creation
    // should not be deleted, but when pulled from the DB it will use the Setter to
    // replace this value
    // in the case that it is deleted.
    @Column(nullable = false)
    private boolean deleted = false;

    @Embedded
    private Profile profile;

    // One thing that could be helpful for all of your Lists is initializing them to
    // empty lists.
    // This will help you to avoid null pointer exceptions later on.
    // I'm only doing it for this List, but feel free to do it for all lists.
    @OneToMany(mappedBy = "author")
    private List<Tweet> tweets = new ArrayList<>();

    // many to many for followers and following
    @ManyToMany
    @JoinTable(name = "followers_following", joinColumns = @JoinColumn(name = "following_id"), inverseJoinColumns = @JoinColumn(name = "follower_id"))
    private List<User> following = new ArrayList<>();

    // inverse for followers following
    @ManyToMany(mappedBy = "following")
    private List<User> followers = new ArrayList<>();

    // many to many for user likes
    @ManyToMany(mappedBy = "likingUsers")
    private List<Tweet> likedTweets = new ArrayList<>();

    // many to many iverse side for mentioned users
    @ManyToMany(mappedBy = "mentionedUsers")
    private List<Tweet> mentioningTweets = new ArrayList<>();

}
