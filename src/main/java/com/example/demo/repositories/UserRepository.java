package com.example.demo.repositories;

import java.util.List;
import java.util.Optional;

import com.example.demo.entities.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByCredentialsUsername(String name);

    List<User> findAllByDeletedFalse();
}
