package com.stockgame.domain.repository;

import com.stockgame.domain.entity.User;
import com.stockgame.domain.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    /** provider + providerId 조합으로 소셜 계정을 특정합니다. */
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
}

