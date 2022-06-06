package com.example.demo.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
public class Tweet implements Deletable {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User author;

    @CreationTimestamp
    private Timestamp posted;

    @Column(nullable = false)
    private boolean deleted;

    private String content;

    @ManyToOne
    @JoinColumn(name = "tweet_reply_of_id")
    private Tweet inReplyTo;

    @ManyToOne
    @JoinColumn(name = "tweet_reposted_from_id")
    private Tweet repostOf;

    @OneToMany(mappedBy = "inReplyTo")
    private List<Tweet> replies = new ArrayList<>();

    @OneToMany(mappedBy = "repostOf")
    private List<Tweet> reposts = new ArrayList<>();

    // Many to Many for tweet_hashtags relationship
    // Do the join columns need to be in any specific order?
    // are my tweet_id and hashtag_id backwards? does it even matter?
    // These are in the correct order, though they aren't necessary...
    // JPA uses the names of the fields as the names of the columns. You're just
    // overriding those names.
    // This is totally acceptable. Especially if you prefer the _id format.
    @ManyToMany
    @JoinTable(name = "tweet_hashtags", joinColumns = @JoinColumn(name = "tweet_id"), inverseJoinColumns = @JoinColumn(name = "hashtag_id"))
    private List<Hashtag> hashtags = new ArrayList<>();

    // Many to Many for user_mentions relationship
    // same as above
    @ManyToMany
    @JoinTable(name = "user_mentions", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "tweet_id"))
    private List<User> mentionedUsers = new ArrayList<>();

    // many to many for user_likes on this side
    @ManyToMany
    @JoinTable(name = "user_likes", joinColumns = @JoinColumn(name = "tweet_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> likingUsers = new ArrayList<>();

}
