package com.stockgame.domain.repository;

import com.stockgame.domain.entity.User;
import com.stockgame.domain.enums.AuthProvider;
import com.stockgame.dto.RankingEntryDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    @Query("""
            select new com.stockgame.dto.RankingEntryDto(0, u.nickname, u.points)
            from User u
            order by u.points desc
            """)
    List<RankingEntryDto> findTopRankingEntries(Pageable pageable);

    @Query("select u.points from User u where u.id = :id")
    Optional<Long> findPointsById(Long id);

    @Query("select u.points from User u where u.email = :email")
    Optional<Long> findPointsByEmail(String email);

    long countByPointsGreaterThan(Long points);
}
