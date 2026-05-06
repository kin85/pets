package com.project.pets.repository;

import com.project.pets.domain.UserToken;
import com.project.pets.domain.enums.UserTokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

    Optional<UserToken> findByTokenAndType(String token, UserTokenType type);

    void deleteByUser_IdAndType(Long userId, UserTokenType type);
}
