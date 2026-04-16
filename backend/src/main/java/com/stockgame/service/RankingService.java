package com.stockgame.service;

import com.stockgame.domain.entity.User;
import com.stockgame.domain.repository.UserRepository;
import com.stockgame.dto.MyRankingDto;
import com.stockgame.dto.RankingEntryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<RankingEntryDto> getTopRankings(int limit) {
        List<User> topUsers = userRepository.findByOrderByPointsDesc(PageRequest.of(0, limit));

        List<RankingEntryDto> rankings = new ArrayList<>();
        long currentRank = 0;
        Long prevPoints = null;

        for (int i = 0; i < topUsers.size(); i++) {
            User u = topUsers.get(i);
            if (prevPoints == null || !u.getPoints().equals(prevPoints)) {
                currentRank = i + 1;
            }
            prevPoints = u.getPoints();

            rankings.add(RankingEntryDto.builder()
                    .rank(currentRank)
                    .nickname(u.getNickname())
                    .points(u.getPoints())
                    .build());
        }

        return rankings;
    }

    @Transactional(readOnly = true)
    public MyRankingDto getMyRanking(Object principal) {
        User user = resolveUser(principal);
        long totalUsers = userRepository.count();
        long rank = userRepository.countByPointsGreaterThan(user.getPoints()) + 1;

        return MyRankingDto.builder()
                .rank(rank)
                .totalUsers(totalUsers)
                .points(user.getPoints())
                .build();
    }

    private User resolveUser(Object principal) {
        if (principal instanceof Long id) {
            return userRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        }
        if (principal instanceof String email && StringUtils.hasText(email)) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED, "인증 정보로 사용자를 찾을 수 없습니다."));
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
    }
}
