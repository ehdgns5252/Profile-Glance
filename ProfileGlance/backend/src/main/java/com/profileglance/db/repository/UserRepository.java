package com.profileglance.db.repository;

import com.profileglance.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserEmail(String email);
    Optional<User> findByUserNickname(String userNickname);
    @Transactional
    void deleteByuserEmail(String userEmail);

    @Transactional
    void deleteByUserNickname(String userNickname);
}