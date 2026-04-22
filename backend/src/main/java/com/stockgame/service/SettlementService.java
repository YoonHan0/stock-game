package com.stockgame.service;

import com.stockgame.domain.entity.StockQuizDaily;
import com.stockgame.domain.entity.User;
import com.stockgame.domain.entity.Vote;
import com.stockgame.domain.repository.MarketHolidayRepository;
import com.stockgame.domain.repository.StockQuizRepository;
import com.stockgame.domain.repository.UserRepository;
import com.stockgame.domain.repository.VoteRepository;
import com.stockgame.dto.ScheduleTriggerResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private static final List<String> SETTLEABLE_STATUSES = List.of("OPEN", "CLOSED");

    private final StockQuizRepository quizRepository;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final StockScraper stockScraper;
    private final MarketHolidayRepository holidayRepository;

    @Scheduled(cron = "0 5 20 * * *", zone = "Asia/Seoul")
    @Transactional
    public void settleOpenQuizzes() {
        ScheduleTriggerResponseDto result = triggerSettlement();
        log.info("[{}] {}", result.action(), result.message());
    }

    @Transactional
    public ScheduleTriggerResponseDto triggerSettlement() {
        LocalDate today = currentDate();

        DayOfWeek dow = today.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return new ScheduleTriggerResponseDto(
                    "settle",
                    false,
                    0,
                    "오늘(" + today + ")은 주말이라 정산을 건너뜁니다.");
        }

        if (holidayRepository.existsByHolidayDate(today)) {
            return new ScheduleTriggerResponseDto(
                    "settle",
                    false,
                    0,
                    "오늘(" + today + ")은 휴장일이라 정산을 건너뜁니다.");
        }

        List<StockQuizDaily> quizzesToSettle =
                new ArrayList<>(quizRepository.findByStatusInAndQuizDateBefore(SETTLEABLE_STATUSES, today));

        if (quizzesToSettle.isEmpty()) {
            return new ScheduleTriggerResponseDto(
                    "settle",
                    false,
                    0,
                    "정산할 퀴즈가 없습니다.");
        }

        for (StockQuizDaily quiz : quizzesToSettle) {
            settleQuiz(quiz);
        }

        return new ScheduleTriggerResponseDto(
                "settle",
                true,
                quizzesToSettle.size(),
                "정산 배치 완료: " + quizzesToSettle.size() + "건 처리했습니다.");
    }

    @Transactional
    public ScheduleTriggerResponseDto triggerSettleToday() {
        LocalDate today = currentDate();
        StockQuizDaily todayQuiz = quizRepository.findByQuizDate(today).orElse(null);

        if (todayQuiz == null) {
            return new ScheduleTriggerResponseDto(
                    "settle-today",
                    false,
                    0,
                    "오늘(" + today + ") 퀴즈가 없어 당일 정산을 건너뜁니다.");
        }

        String currentStatus = todayQuiz.getStatus();
        if (!isSettleableStatus(currentStatus)) {
            return new ScheduleTriggerResponseDto(
                    "settle-today",
                    false,
                    0,
                    "오늘(" + today + ") 퀴즈는 현재 상태가 " + currentStatus
                            + " 이라 당일 정산 대상이 아닙니다. 정산 가능 상태="
                            + String.join(", ", SETTLEABLE_STATUSES));
        }

        Long quizId = todayQuiz.getQuizId();
        settleQuiz(todayQuiz);
        return new ScheduleTriggerResponseDto(
                "settle-today",
                true,
                1,
                "오늘(" + today + ") 퀴즈 당일 정산 완료: quizId=" + quizId
                        + ", previousStatus=" + currentStatus + ", currentStatus=SETTLED");
    }

    private boolean isSettleableStatus(String status) {
        return SETTLEABLE_STATUSES.contains(status);
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
        quiz.setStatus("SETTLED");
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

    LocalDate currentDate() {
        return LocalDate.now();
    }
}
