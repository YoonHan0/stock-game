# Stock Game 기능 설계서

## 1. 프로젝트 개요

Stock Game은 사용자가 매일 1개의 주식 종목에 대해 다음 거래일 종가가 상승할지 하락할지 예측하는 웹 서비스입니다. 현재 코드는 인증된 사용자가 오늘의 퀴즈를 조회하고 1회 투표한 뒤 전체 참여 통계를 확인하는 MVP 범위에 집중되어 있습니다.

### 현재 구현 범위

- 이메일/비밀번호 회원가입 및 로그인
- JWT 쿠키 기반 인증 상태 유지
- Google OAuth2 로그인 후 JWT 쿠키 발급 구조
- 오늘의 퀴즈 1건 조회
- 퀴즈 1일 1회 투표
- 상승/하락 실시간 집계 조회
- 서버 시작 시 오늘의 퀴즈 자동 생성
- 네이버 금융 스크래핑 기반 현재가 조회

### README 기준 목표/확장 범위

- 포인트 적립 및 마이페이지
- 랭킹 및 적중률 통계
- 종가 정산 자동화
- 휴장일 대응 스케줄링
- 소셜 로그인 제공자 확장

## 2. 기술 스택

### 프론트엔드

| 구분 | 내용 |
| --- | --- |
| 런타임/빌드 | React 19, Vite 7 |
| 라우팅 | React Router DOM 7 |
| HTTP 통신 | Axios |
| UI 아이콘 | Lucide React |
| 스타일링 환경 | Tailwind CSS 관련 패키지 포함, 현재 화면은 CSS 파일 기반 구현도 함께 사용 |
| 기타 의존성 | `@supabase/supabase-js` 패키지가 포함되어 있으나 현재 확인된 코드 사용처는 없음 |

### 백엔드

| 구분 | 내용 |
| --- | --- |
| 프레임워크 | Spring Boot 3.5.11 |
| 언어 | Java 21 |
| 웹/API | Spring Web |
| 보안 | Spring Security, OAuth2 Client |
| 데이터 접근 | Spring Data JPA |
| DB 드라이버 | PostgreSQL |
| 인증 토큰 | JJWT |
| HTML 파싱 | Jsoup |
| 보조 도구 | Lombok |

### 환경 설정 소스

- `frontend/package.json`
- `backend/build.gradle`
- `backend/src/main/resources/application.yaml`
- `README.md`

## 3. 시스템 아키텍처

### 구성 요소

| 계층 | 주요 구성 | 역할 |
| --- | --- | --- |
| Frontend | `App`, `AuthContext`, 각 페이지/라우트 컴포넌트 | 인증 상태 복원, 라우팅, 퀴즈 조회/투표 UI |
| Backend API | `AuthController`, `QuizController` | 인증/퀴즈 관련 HTTP 엔드포인트 제공 |
| Service | `AuthService`, `QuizService`, `StockScraper` | 인증 처리, 투표 검증, 시세 조회 |
| Security | `SecurityConfig`, `JwtAuthenticationFilter`, `JwtProvider`, `OAuth2SuccessHandler` | JWT 쿠키 인증, OAuth2 성공 후 후처리 |
| Persistence | JPA Entity/Repository | 사용자, 종목, 일일 퀴즈, 투표 저장 |
| Init/Batch 성격 구성 | `DataInitConfig` | 서버 시작 시 오늘의 퀴즈 자동 생성 |
| 외부 연동 | Google OAuth2, 네이버 금융 HTML | 소셜 로그인, 현재가 스크래핑 |

### 요청 흐름

1. 프론트엔드가 `axios`로 `/api` 하위 엔드포인트를 호출합니다.
2. 로그인 성공 시 백엔드는 `accessToken` HTTP-only 쿠키를 내려줍니다.
3. 이후 브라우저는 `withCredentials: true` 설정에 따라 쿠키를 자동 전송합니다.
4. `JwtAuthenticationFilter`가 쿠키의 JWT를 검증하고 `SecurityContext`에 인증 객체를 세팅합니다.
5. 컨트롤러와 서비스가 인증 사용자 기준으로 퀴즈 조회, 투표 저장, 통계 조회를 수행합니다.

### 초기화 흐름

1. 애플리케이션 시작 시 `DataInitConfig`의 `CommandLineRunner`가 실행됩니다.
2. 오늘 날짜(`LocalDate.now()`)의 퀴즈 존재 여부를 먼저 확인합니다.
3. 없으면 `is_active=true` 종목 목록을 `stock_code` 오름차순으로 조회합니다.
4. 첫 번째 활성 종목의 현재가를 스크래핑합니다.
5. 기준가(`base_price`)와 상태 `OPEN`으로 `stock_quiz_daily` 레코드를 생성합니다.

## 4. 현재 구현된 사용자 기능

### 사용자 시나리오 요약

| 시나리오 | 현재 구현 상태 | 설명 |
| --- | --- | --- |
| 비회원 진입 | 구현 | 앱 시작 시 인증 복원 실패 시 로그인 화면으로 유도 |
| 회원가입 | 구현 | 이메일/비밀번호/닉네임 입력 후 즉시 자동 로그인 |
| 이메일 로그인 | 구현 | 로그인 성공 시 JWT 쿠키 발급 및 홈 진입 |
| 소셜 로그인 | 부분 구현 | UI에는 Google/Kakao 버튼이 있으나 서버 사용자 동기화는 Google만 지원 |
| 오늘의 퀴즈 조회 | 구현 | 인증 사용자가 홈에서 당일 퀴즈와 현재가 조회 |
| 오늘의 퀴즈 투표 | 구현 | 상승/하락 중 1회 선택 가능 |
| 실시간 통계 확인 | 구현 | 투표 전후 모두 집계 바 형태로 비율 표시 |
| 로그아웃 | 구현 | 쿠키 만료 후 로그인 화면으로 이동 |
| 포인트/랭킹/전적 조회 | 미구현 | README 목표에만 존재 |

### 사용자 기능 상세 흐름

1. 사용자가 앱에 접속하면 `AuthContext`가 `/api/auth/me`로 인증 복원을 시도합니다.
2. 미인증 상태면 `/login`, 인증 상태면 `/`로 흐름이 정리됩니다.
3. 홈 진입 후 오늘의 퀴즈를 조회하고, 이어 투표 여부와 통계를 추가 조회합니다.
4. 아직 투표하지 않았다면 상승/하락 중 하나를 선택할 수 있습니다.
5. 투표 완료 후에는 재투표 대신 실시간 통계와 참여 완료 상태만 노출됩니다.

## 5. 프론트엔드 기능 설계

### 라우팅 구조

| 경로 | 컴포넌트 | 접근 제어 | 기능 |
| --- | --- | --- | --- |
| `/login` | `LoginScreen` | `PublicRoute` | 이메일 로그인, 소셜 로그인 진입 |
| `/signup` | `SignupPage` | `PublicRoute` | 이메일 회원가입 |
| `/` | `HomePage` | `ProtectedRoute` | 오늘의 퀴즈 조회, 투표, 통계 확인, 로그아웃 |

### AuthContext

`AuthContext`는 프론트엔드 인증 상태의 단일 진입점입니다.

- 상태
  - `user`
  - `isAuthenticated`
  - `isAuthLoading`
- 주요 함수
  - `restoreAuth()`: `/api/auth/me` 호출로 새로고침 후 인증 복원
  - `loginWithEmail()`: `/api/auth/login` 호출 후 `/api/auth/me`로 사용자 정보 재조회
  - `signupWithEmail()`: `/api/auth/signup` 호출
  - `logout()`: `/api/auth/logout` 호출 후 사용자 상태 제거

### ProtectedRoute / PublicRoute

| 컴포넌트 | 동작 |
| --- | --- |
| `ProtectedRoute` | 인증 로딩 중에는 안내 문구를 보여주고, 미인증이면 `/login`으로 리다이렉트 |
| `PublicRoute` | 인증 로딩 중에는 안내 문구를 보여주고, 인증 상태면 `/`로 리다이렉트 |

### `/login` 흐름

1. 사용자가 이메일/비밀번호를 입력합니다.
2. 값이 비어 있으면 경고 다이얼로그를 표시합니다.
3. `loginWithEmail()` 호출 성공 시 `/`로 이동합니다.
4. 실패 시 “이메일/비밀번호를 확인해 달라”는 오류 다이얼로그를 표시합니다.
5. Google/Kakao 버튼은 `getSocialLoginUrl(provider)`가 만든 URL로 전체 페이지 이동합니다.

> 주의: 프론트 기본 소셜 로그인 URL은 `/api/auth/oauth2/authorization/{provider}` 형식이지만, 백엔드 보안 설정은 `/oauth2/**`와 `/login/**`를 허용합니다. 실제 OAuth 진입 URL은 배포/프록시 구성과 함께 확인이 필요합니다.

### `/signup` 흐름

1. 이메일, 비밀번호, 닉네임을 모두 입력해야 합니다.
2. `/api/auth/signup` 성공 후 같은 자격정보로 `loginWithEmail()`을 다시 호출합니다.
3. 자동 로그인에 성공하면 `/`로 이동합니다.
4. 실패 시 경고/오류 다이얼로그를 표시합니다.

### `/`(HomePage) 흐름

#### 1) 퀴즈 조회

1. `HomePage` 마운트 시 `fetchQuiz()`를 실행합니다.
2. `getTodayQuiz()`로 `/api/quiz/today`를 호출합니다.
3. 응답에 `quizId`가 있으면 `Promise.all`로 다음 두 요청을 병렬 호출합니다.
   - `getIsVoteState(quizId)` → `/api/quiz/{quizId}/check-vote`
   - `getVoteStats(quizId)` → `/api/quiz/{quizId}/stats`
4. 퀴즈가 없으면 경고 알림과 “진행 중인 퀴즈 없음” 화면을 표시합니다.

#### 2) 투표

1. 사용자가 상승(`UP`) 또는 하락(`DOWN`) 버튼을 누릅니다.
2. 투표 직전에 다시 `check-vote`를 호출해 중복 참여를 한 번 더 확인합니다.
3. 중복이 아니면 `/api/quiz/vote`를 호출합니다.
4. 성공 시 `voted=true`로 전환하고 통계를 다시 조회합니다.
5. 실패 시 서버 상태코드와 에러 코드를 기반으로 메시지를 분기합니다.

#### 3) 통계

- `VoteBar` 컴포넌트가 `upCount`, `downCount`, `upPercentage`, `downPercentage`를 시각화합니다.
- 총 참여자가 0명이면 빈 막대와 “아직 참여자 없음” 문구를 보여줍니다.
- 이미 투표한 사용자는 “참여 완료” 칩과 함께 통계 전용 화면을 봅니다.

#### 4) 로그아웃

1. 로그아웃 버튼 클릭 시 `AuthContext.logout()`을 호출합니다.
2. 백엔드가 만료 쿠키를 내려주면 프론트 상태를 비웁니다.
3. 이후 `/login`으로 이동합니다.

### 화면 상태 처리

| 상태 | 처리 방식 |
| --- | --- |
| 인증 확인 중 | `ProtectedRoute`/`PublicRoute`에서 안내 문구 표시 |
| 퀴즈 로딩 중 | 로딩 카드와 정보 알림 표시 |
| 퀴즈 없음 | 경고/오류 메시지와 다시 불러오기 버튼 표시 |
| 중복 투표 | `ALREADY_VOTED` 코드 기준 경고 알림 |
| 투표 가능 시간 아님 | `VOTE_TIME_RESTRICTED` 코드 기준 경고 알림 |
| 퀴즈 마감 | `QUIZ_CLOSED` 코드 기준 경고 알림 |

## 6. 백엔드 기능 설계

### AuthController

| 엔드포인트 | 설명 |
| --- | --- |
| `POST /api/auth/signup` | 회원가입 요청을 받아 `AuthService.signup()` 실행 후 생성된 사용자 정보 반환 |
| `POST /api/auth/login` | 로그인 성공 시 사용자 정보와 `Set-Cookie` 헤더 반환 |
| `POST /api/auth/logout` | 만료된 `accessToken` 쿠키를 내려 로그아웃 처리 |
| `GET /api/auth/me` | 현재 인증 principal을 `User`로 해석해 사용자 정보 반환 |

추가로 `AuthBadRequestException`, `AuthUnauthorizedException`을 각각 `400`, `401` 응답으로 변환합니다.

### QuizController

| 엔드포인트 | 설명 |
| --- | --- |
| `GET /api/quiz/today` | 오늘의 퀴즈와 현재가를 조회해 `QuizResponseDto`로 반환 |
| `POST /api/quiz/vote` | 현재 사용자 기준 투표 저장 |
| `GET /api/quiz/{quizId}/stats` | 상승/하락 집계 반환 |
| `GET /api/quiz/{quizId}/check-vote` | 현재 사용자의 해당 퀴즈 투표 여부 반환 |

`QuizVoteConflictException`은 `409 Conflict`와 에러 코드(`ALREADY_VOTED`, `VOTE_TIME_RESTRICTED`, `QUIZ_CLOSED`)로 내려갑니다.

### AuthService

주요 책임은 로컬 계정 회원가입과 로그인 처리입니다.

- `signup()`
  - 이메일 소문자/trim 정규화
  - 이메일/비밀번호 필수값 검사
  - 이메일 중복 검사
  - 닉네임이 비어 있으면 이메일 아이디 부분으로 대체
  - `BCryptPasswordEncoder`로 비밀번호 해시 저장
  - `User.ofLocal()`로 사용자 생성
- `login()`
  - 사용자 조회
  - 로컬 계정 여부 확인(`passwordHash` 존재 여부)
  - 비밀번호 검증
  - `JwtProvider`로 JWT 생성
  - HTTP-only 쿠키 생성 후 반환
- `logoutCookie()`
  - 만료 쿠키 생성

### QuizService

주요 책임은 퀴즈 조회와 투표 제약 검증입니다.

- `getTodayQuiz()`: 오늘 날짜의 퀴즈 1건 조회
- `getCurrentLivePrice(stockCode)`: `StockScraper` 위임
- `isMarketClosed()`: 현재 시각이 16:00 이후인지 판별
- `saveVote()`
  - principal에서 사용자 식별
  - 투표 가능 시간(09:00~23:59:59) 검증
  - 퀴즈 존재 여부 검증
  - 퀴즈 상태가 `OPEN`인지 확인
  - 같은 사용자 중복 투표 검사
  - `Vote` 저장
- `getVoteStats()`
  - `UP`, `DOWN` 건수 집계
  - 소수점 첫째 자리까지 비율 계산
- `getIsVoteState()`
  - 현재 사용자의 투표 여부 반환

### SecurityConfig

보안 정책의 중심 구성입니다.

- CSRF 비활성화
- 세션 정책 `STATELESS`
- CORS 허용 출처를 `http://localhost:5173`로 설정
- `/api/auth/**`, `/login/**`, `/oauth2/**`, `/error`, 모든 `OPTIONS` 요청을 인증 없이 허용
- 그 외 요청은 인증 필요
- `/api/**` 미인증 접근 시 리다이렉트 대신 `401`
- `/api/**` 권한 부족 시 `403`
- OAuth2 로그인 시 `CustomOAuth2UserService`와 `OAuth2SuccessHandler` 연결
- `JwtAuthenticationFilter`를 `UsernamePasswordAuthenticationFilter` 앞에 배치

### JwtAuthenticationFilter

- 요청 쿠키에서 `accessToken`을 찾습니다.
- 토큰이 존재하고 유효하면 `userId`, `email`, `role` 클레임을 읽습니다.
- principal은 이메일이 있으면 이메일, 없으면 사용자 ID를 사용합니다.
- 최종적으로 `UsernamePasswordAuthenticationToken`을 `SecurityContext`에 저장합니다.

### JwtProvider

- HMAC 기반 JWT 생성/검증 담당
- 사용자 ID, 이메일, 역할을 클레임에 저장
- 액세스 토큰 유효기간은 코드상 1시간으로 고정
- `accessToken` HTTP-only 쿠키 생성
- 로그아웃용 만료 쿠키 생성

### OAuth2SuccessHandler

- OAuth2 로그인 성공 후 `OAuth2User`에서 이메일을 추출합니다.
- 이메일 기준으로 DB 사용자를 다시 조회합니다.
- 조회된 사용자 정보를 바탕으로 JWT를 생성하고 쿠키를 응답 헤더에 추가합니다.
- 이후 프론트엔드 URL로 리다이렉트합니다.

### DataInitConfig

- 애플리케이션 시작 시 오늘 퀴즈가 있는지 확인합니다.
- 없으면 활성 종목 목록 중 첫 번째 종목을 선택합니다.
- 스크래핑한 현재가를 기준가로 저장합니다.
- 상태는 `OPEN`으로 시작합니다.

## 7. 데이터 모델 개요

### 엔티티 요약

| 엔티티 | 주요 필드 | 역할 |
| --- | --- | --- |
| `User` | `id`, `email`, `passwordHash`, `nickname`, `provider`, `providerId`, `role`, `points` | 로컬/소셜 계정을 통합 저장 |
| `Stock` | `stockCode`, `stockName`, `isActive` | 퀴즈 출제 대상 종목 마스터 |
| `StockQuizDaily` | `quizId`, `quizDate`, `stock`, `base_price`, `final_price`, `status` | 특정 날짜의 일일 퀴즈 |
| `Vote` | `vote_id`, `userId`, `quiz`, `prediction`, `is_correct`, `created_at` | 사용자별 퀴즈 참여 기록 |

### 관계 개요

| 관계 | 설명 |
| --- | --- |
| `Stock` 1 : N `StockQuizDaily` | 하나의 종목이 여러 날짜의 퀴즈로 선택될 수 있음 |
| `StockQuizDaily` 1 : N `Vote` | 하나의 일일 퀴즈에 여러 투표가 연결됨 |
| `User` 1 : N `Vote` | 개념상 사용자 1명이 여러 투표를 가질 수 있으나, 엔티티 매핑은 `Vote.userId` 값으로만 저장 |

### 구현 관점 메모

- `Vote` 테이블은 `(quiz_id, user_id)` 유니크 제약으로 하루 1회 투표를 보장합니다.
- `User.points`, `StockQuizDaily.final_price`, `Vote.is_correct`는 존재하지만 현재 핵심 로직에서 적극 사용되지는 않습니다.
- 사용자 권한은 `USER`, `ADMIN` enum으로 준비되어 있으나 현재 역할 기반 기능 분리는 없습니다.

## 8. 인증/인가 및 세션 흐름

### 로컬 로그인 흐름

1. 사용자가 `/login`에서 이메일과 비밀번호를 제출합니다.
2. `AuthController.login()`이 `AuthService.login()`을 호출합니다.
3. 서비스가 비밀번호를 검증하고 JWT를 발급합니다.
4. JWT는 `accessToken` HTTP-only 쿠키로 응답됩니다.
5. 프론트는 필요 시 `/api/auth/me`를 호출해 현재 사용자 정보를 복원합니다.

### 인증 유지 흐름

1. 브라우저는 이후 요청마다 쿠키를 자동 전송합니다.
2. `JwtAuthenticationFilter`가 토큰을 읽고 검증합니다.
3. 검증 성공 시 `SecurityContext`에 인증 정보가 저장됩니다.
4. 보호된 API는 이 인증 정보를 바탕으로 사용자별 로직을 수행합니다.

### 로그아웃 흐름

1. 프론트가 `/api/auth/logout`을 호출합니다.
2. 서버는 `maxAge=0`인 만료 쿠키를 내려줍니다.
3. 프론트는 로컬 사용자 상태를 비우고 로그인 화면으로 이동합니다.

### OAuth2 흐름

1. 사용자가 소셜 로그인 버튼을 클릭합니다.
2. OAuth2 인증이 끝나면 `CustomOAuth2UserService`가 사용자 정보를 DB와 동기화합니다.
3. `OAuth2SuccessHandler`가 JWT 쿠키를 발급합니다.
4. 이후 프론트엔드 URL로 리다이렉트합니다.

### 현재 인가 정책

- `/api/auth/**`는 로그인 없이 접근 가능합니다.
- 퀴즈 관련 API는 인증이 필요합니다.
- 역할 클레임은 JWT에 포함되지만, 현재는 역할에 따른 URL 분리 정책이 없습니다.
- 세션은 서버에 저장하지 않는 무상태(`STATELESS`) 방식입니다.

## 9. API 개요

### 인증 API

| 메서드 | 경로 | 요청/응답 개요 | 인증 |
| --- | --- | --- | --- |
| POST | `/api/auth/signup` | 이메일, 비밀번호, 닉네임을 받아 사용자 생성 후 기본 정보 반환 | 불필요 |
| POST | `/api/auth/login` | 이메일, 비밀번호 검증 후 사용자 정보 반환 + JWT 쿠키 발급 | 불필요 |
| POST | `/api/auth/logout` | 만료 쿠키 반환 | 불필요 |
| GET | `/api/auth/me` | 현재 로그인 사용자 정보 반환 | 필요 |

### 퀴즈 API

| 메서드 | 경로 | 요청/응답 개요 | 인증 |
| --- | --- | --- | --- |
| GET | `/api/quiz/today` | 오늘의 퀴즈 ID, 종목명, 종목코드, 기준가, 현재가, 장 마감 여부 반환 | 필요 |
| POST | `/api/quiz/vote` | `quizId`, `prediction`을 받아 투표 저장 | 필요 |
| GET | `/api/quiz/{quizId}/stats` | 상승/하락 건수와 비율 반환 | 필요 |
| GET | `/api/quiz/{quizId}/check-vote` | 현재 사용자의 투표 여부(Boolean) 반환 | 필요 |

### 소셜 로그인 관련 경로 메모

| 구분 | 코드상 확인 내용 |
| --- | --- |
| 프론트 기본 생성 URL | `/api/auth/oauth2/authorization/{provider}` |
| 보안 허용 패턴 | `/oauth2/**`, `/login/**`, `/api/auth/**` |
| 실제 동기화 지원 provider | Google |

### 오류 응답

| 상황 | 응답 |
| --- | --- |
| 인증 실패 | `401 Unauthorized` |
| 권한 부족 | `403 Forbidden` |
| 잘못된 회원가입/로그인 입력 | `400 Bad Request` |
| 중복 투표/시간 제한/마감 퀴즈 | `409 Conflict` + 에러 코드 |

## 10. 운영/환경 설정 포인트

### 프론트엔드 환경 포인트

- `VITE_API_BASE_URL` 기본값은 `http://localhost:8080/api`
- 소셜 로그인 URL도 환경변수로 재정의 가능
  - `VITE_SOCIAL_LOGIN_GOOGLE_URL`
  - `VITE_SOCIAL_LOGIN_KAKAO_URL`
  - `VITE_SOCIAL_LOGIN_NAVER_URL`
- Axios는 `withCredentials: true`로 쿠키 기반 인증을 사용합니다.

### 백엔드 환경 포인트

- `application.yaml` 기준 주요 환경변수
  - `GOOGLE_CLIENT_ID`
  - `GOOGLE_CLIENT_SECRET`
  - `DB_URL`
  - `DB_USERNAME`
  - `DB_PASSWORD`
  - `JWT_SECRET`
  - `JWT_ISSUER`
  - `FRONTEND_URL`
- 서버 포트는 `8080`
- JPA `ddl-auto`는 `validate`
- SQL 로그 출력 활성화(`show-sql: true`)
- 쿠키 기본 설정
  - `secure: false`
  - `same-site: Lax`

### 코드와 설정 사이에서 확인할 점

- `application.yaml`에는 `supabase.jwt.secret`, `supabase.jwt.issuer` 구조가 보이지만, `JwtProvider`는 실제로 `@Value("${JWT_SECRET}")`를 사용합니다. 즉 현재 JWT 서명키는 루트 환경변수 `JWT_SECRET` 의존으로 보는 것이 맞습니다.
- README에는 Supabase Auth 기반 설명이 남아 있으나, 현재 구현된 인증 흐름은 Spring Security + 자체 JWT 쿠키 기반 로컬 로그인과 Google OAuth2입니다.
- CORS 허용 출처가 `http://localhost:5173`로 고정되어 있어 배포 환경에서는 추가 설정이 필요합니다.

## 11. README 기준 예정 기능 및 확장 포인트

### README에 명시됐지만 현재 코드에서 완성되지 않은 기능

| 항목 | README 기준 기대 | 현재 코드 기준 상태 |
| --- | --- | --- |
| 포인트 시스템 | 투표 결과에 따른 포인트 지급, 마이페이지 표시 | `User.points` 필드는 있으나 적립/차감 로직과 화면 없음 |
| 실시간 랭킹 | 누적 포인트 순 상위 사용자 제공 | 관련 API/화면/쿼리 없음 |
| 적중률 통계 | 과거 투표 기반 승률 계산 및 시각화 | 관련 API/화면 없음 |
| 자동 정산 | 전일/금일 종가 비교 후 정답 판정 | `final_price`, `is_correct` 필드는 있으나 정산 배치 없음 |
| 휴장일 대응 | 공휴일/휴장일에 맞춘 스케줄 제어 | 관련 스케줄러/정책 없음 |
| 추가 소셜 로그인 | Kakao 등 확장 | 프론트 버튼은 있으나 서버 사용자 동기화는 Google만 지원 |

### 확장 설계 포인트

1. **정산 배치 도입**
   - 퀴즈 마감 시 `final_price`를 저장하고 `Vote.is_correct`를 갱신할 배치가 필요합니다.
2. **포인트/전적 조회 API 추가**
   - `User.points`를 실제 기능으로 연결하려면 사용자별 히스토리와 집계 API가 필요합니다.
3. **퀴즈 생성 정책 고도화**
   - 현재는 첫 번째 활성 종목 고정이므로, 회전 정책이나 추천 정책이 필요합니다.
4. **소셜 로그인 경로 정리**
   - 프론트 URL 구성과 Spring Security 실제 진입 경로를 일치시키는 점검이 필요합니다.
5. **운영 환경 보안 강화**
   - 쿠키 `secure=true`, 배포 도메인 CORS, OAuth 리디렉션 URL 관리를 환경별로 분리할 필요가 있습니다.
