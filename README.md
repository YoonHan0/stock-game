## 프로젝트 구성 및 기술 환경

- 구조: Monorepo (/frontend, /backend)
- Frontend: React (Vite) + JavaScript + Tailwind CSS (Node v16.20.2 대응)
- Backend: Spring Boot 3.x + Java 21 + Gradle (주가 수집: HttpClient 활용)
- DB/Auth: Supabase (PostgreSQL + Auth)

## 주요 요구사항

1. 사용자 인증 및 프로필 (Auth)
소셜 로그인: Supabase Auth를 이용한 Google 로그인.
JWT 검증: 프론트엔드에서 발행된 Supabase JWT를 백엔드(Java)에서 수신하여 유효성 검증 및 사용자 식별.
포인트 시스템: 투표 결과에 따른 포인트 지급 및 마이페이지 내 표시.

2. 주식 퀴즈 시스템 (Core Game)
퀴즈 생성: 백엔드에서 매일 특정 종목을 선정하여 '오늘의 퀴즈'로 등록.
실시간 투표: 사용자가 해당 종목의 '상승' 또는 '하락'을 선택 (하루 1회 제한).
투표 현황: 현재 다른 사용자들이 어디에 더 많이 투표했는지 비율 노출.

3. 데이터 수집 및 정산 자동화 (Backend Batch)
데이터 스크래핑: Java 21의 HttpClient를 사용하여 금융 사이트(네이버 금융 등)에서 실시간/종가 데이터 수집.
자동 정산: 전일 종가와 금일 종가를 비교하여 사용자의 예측을 확인하고 정답 판정.
휴장일 대응: 한국거래소 휴장일 데이터 또는 공휴일 체크 로직을 통해 스케줄러 제어.

4. 랭킹 및 통계
실시간 랭킹: 누적 포인트 순으로 상위 사용자 리스트 제공.
적중률 통계: 사용자의 과거 투표 이력을 바탕으로 승률 계산 및 시각화.

## 오류내용 정리
- 웹페이지 호출 시 `302 Error`
1. 브라우저가 `/api/quiz/today`와 같은 특정 경로로 요청을 보냄
2. 서버가 200 OK 대신 302 리다이렉트 응답을 보냄 (보통 로그인 페이지나 보안 필터에 의해 리다이렉트되는 경우)
- 발생이유
프로젝트 내에 `Spring Security` 의존성이 추가되어 있는데 별도의 `SecurityConfig`와 같은 처리나 아직 구현 단계가 아니라면 메인 클래스에서 제외 설정(Exclude)를 해야 함.
```java
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class }) // 시큐리티 자동 설정 제외
public class StockgameApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockgameApplication.class, args);
    }
}
```