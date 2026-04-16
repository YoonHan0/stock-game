package com.stockgame.service;

import com.stockgame.domain.entity.StockQuizDaily;
import com.stockgame.domain.entity.User;
import com.stockgame.domain.entity.Vote;
import com.stockgame.domain.repository.MarketHolidayRepository;
import com.stockgame.domain.repository.StockQuizRepository;
import com.stockgame.domain.repository.UserRepository;
import com.stockgame.domain.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private final StockQuizRepository quizRepository;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final StockScraper stockScraper;
    private final MarketHolidayRepository holidayRepository;

    @Scheduled(cron = "0 5 20 * * *", zone = "Asia/Seoul")
    @Transactional
    public void settleOpenQuizzes() {
        LocalDate today = LocalDate.now();

        DayOfWeek dow = today.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            log.info("오늘({})은 주말입니다. 정산을 건너뜁니다.", today);
            return;
        }

        if (holidayRepository.existsByHolidayDate(today)) {
            log.info("오늘({})은 휴장일입니다. 정산을 건너뜁니다.", today);
            return;
        }

        List<StockQuizDaily> openQuizzes = quizRepository.findByStatusAndQuizDateBefore("OPEN", today);

        if (openQuizzes.isEmpty()) {
            log.info("정산할 퀴즈가 없습니다.");
            return;
        }

        log.info("정산 대상 퀴즈 {}건 발견", openQuizzes.size());

        for (StockQuizDaily quiz : openQuizzes) {
            settleQuiz(quiz);
        }

        log.info("정산 배치 완료: {}건 처리", openQuizzes.size());
    }

    private void settleQuiz(StockQuizDaily quiz) {
        String stockCode = quiz.getStock().getStockCode();
        BigDecimal finalPrice = stockScraper.getStockPrice(stockCode);

        quiz.setFinalPrice(finalPrice);

        int comparison = finalPrice.compareTo(quiz.getBase_price());
        String quizResult;
        if (comparison > 0) {
            quizResult = "UP";
        } else if (comparison < 0) {
            quizResult = "DOWN";
        } else {
            quizResult = "FLAT";
        }

        quiz.setQuizResult(quizResult);
        quiz.setStatus("CLOSED");
        quiz.setSettledAt(LocalDateTime.now());

        List<Vote> votes = voteRepository.findByQuizQuizId(quiz.getQuizId());

        Set<Long> userIds = votes.stream()
                .map(Vote::getUserId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        for (Vote vote : votes) {
            int points;
            if ("FLAT".equals(quizResult)) {
                vote.setIsCorrect(null);
                points = 1;
            } else if (vote.getPrediction().equals(quizResult)) {
                vote.setIsCorrect(true);
                points = 2;
            } else {
                vote.setIsCorrect(false);
                points = 0;
            }
            vote.setPointsEarned(points);

            if (points > 0) {
                User user = userMap.get(vote.getUserId());
                if (user != null) {
                    user.addPoints((long) points);
                }
            }
        }

        log.info("퀴즈 정산 완료: quizId={}, date={}, result={}, base={}, final={}",
                quiz.getQuizId(), quiz.getQuizDate(), quizResult,
                quiz.getBase_price(), finalPrice);
    }
}
