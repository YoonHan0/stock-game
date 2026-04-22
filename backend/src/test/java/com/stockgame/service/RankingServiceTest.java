package com.stockgame.service;

import com.stockgame.domain.repository.UserRepository;
import com.stockgame.dto.MyRankingDto;
import com.stockgame.dto.RankingEntryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private UserRepository userRepository;

    private RankingService rankingService;

    @BeforeEach
    void setUp() {
        rankingService = new RankingService(userRepository);
    }

    @Test
    void getTopRankings_assignsSameRankForSamePoints() {
        List<RankingEntryDto> entries = List.of(
                new RankingEntryDto(0, "alpha", 100L),
                new RankingEntryDto(0, "beta", 100L),
                new RankingEntryDto(0, "gamma", 80L)
        );

        when(userRepository.findTopRankingEntries(any())).thenReturn(entries);

        List<RankingEntryDto> result = rankingService.getTopRankings(3);

        assertThat(result).extracting(RankingEntryDto::getRank)
                .containsExactly(1L, 1L, 3L);
        assertThat(result).extracting(RankingEntryDto::getNickname)
                .containsExactly("alpha", "beta", "gamma");
    }

    @Test
    void getMyRanking_usesScalarPointQueryForEmailPrincipal() {
        when(userRepository.findPointsByEmail("me@example.com")).thenReturn(Optional.of(42L));
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByPointsGreaterThan(42L)).thenReturn(2L);

        MyRankingDto result = rankingService.getMyRanking("me@example.com");

        assertThat(result.getRank()).isEqualTo(3L);
        assertThat(result.getTotalUsers()).isEqualTo(10L);
        assertThat(result.getPoints()).isEqualTo(42L);
    }
}
