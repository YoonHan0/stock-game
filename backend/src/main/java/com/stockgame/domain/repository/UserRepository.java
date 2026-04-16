package com.stockgame.domain.repository;

import com.stockgame.domain.entity.User;
import com.stockgame.domain.enums.AuthProvider;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    List<User> findByOrderByPointsDesc(Pageable pageable);

    long countByPointsGreaterThan(Long points);
}

