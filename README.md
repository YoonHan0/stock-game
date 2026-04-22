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

## 백엔드 로컬 실행 및 수동 테스트

- 로컬 수동 검증은 `local` 프로필 기준으로 진행한다.
- `backend/src/main/resources/application-local.yaml`에는 JWT/OAuth 관련 로컬 값이 있고, DB 연결 정보는 `backend/src/main/resources/application.yaml`의 `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`를 반드시 환경변수로 주입해야 한다.
- 로컬 전용 수동 테스트 API는 `LocalScheduleController`에 있으며 `@Profile("local")`로 제한되어 있어 `local` 프로필에서만 노출된다.
- 현재 README에서는 전체 테스트 실행보다 로컬 프로필 실행 + 수동 API 검증 흐름을 우선 안내한다.

### 1. 백엔드 실행 방법

#### IntelliJ 실행
1. `backend` 모듈에서 `com.stockgame.StockgameApplication` 실행 구성을 만든다.
2. Active Profile 또는 실행 인자에 `local`을 지정한다.
    ```bash
    # 실행인자의 program arguments란에 추가
    --spring.profiles.active=local
    ```
3. 아래 환경변수를 설정한 뒤 실행한다.

필수 환경변수
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

#### CLI 실행
```bash
# 1. 백엔드 디렉터리로 이동
cd backend

# 2. 아래 api 호출
```

### 2. 로컬 전용 수동 테스트 API

기본 URL: `http://localhost:8080/api/local/schedules`

| API | 용도 |
| --- | --- |
| `POST /create-quiz` | 오늘 퀴즈를 수동 생성한다. 이미 존재하거나, 주말/휴장일이면 생성하지 않는다. |
| `POST /close-quiz` | 오늘 퀴즈가 `OPEN` 상태일 때 투표를 종료하고 `CLOSED`로 바꾼다. |
| `POST /settle` | 오늘 이전 날짜(`quizDate < today`)의 `OPEN`/`CLOSED` 퀴즈를 정산한다. |
| `POST /settle-today` | 오늘 퀴즈가 `OPEN` 또는 `CLOSED` 상태일 때 당일 강제 정산한다. |

응답은 `action`, `executed`, `affectedCount`, `message` 필드를 반환한다.

### 3. curl 예시

```bash
curl -X POST http://localhost:8080/api/local/schedules/create-quiz
curl -X POST http://localhost:8080/api/local/schedules/close-quiz
curl -X POST http://localhost:8080/api/local/schedules/settle
curl -X POST http://localhost:8080/api/local/schedules/settle-today
```

### 4. 권장 수동 테스트 순서

1. `local` 프로필로 백엔드를 실행하고 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`를 설정한다.
2. `POST /api/local/schedules/create-quiz`로 오늘 퀴즈를 만든다.
3. `POST /api/local/schedules/close-quiz`로 오늘 퀴즈를 마감한다.
4. 같은 날 바로 결과까지 확인하려면 `POST /api/local/schedules/settle-today`를 호출한다.
5. 전일 퀴즈 정산 흐름을 확인할 때는 오늘 이전 날짜 퀴즈가 있는 상태에서 `POST /api/local/schedules/settle`를 호출한다.


---

<br />
<br />
<br />

---

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
