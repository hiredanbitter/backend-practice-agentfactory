package com.example.backend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected Post() {}

    public Post(String title, User user) {
        this.title = title;
        this.user = user;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public User getUser() { return user; }
}
