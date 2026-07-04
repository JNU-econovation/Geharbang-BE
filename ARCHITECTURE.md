
# Geharbang-BE 코드 구조 설명

## 목차

1. [서비스 기능 전체 개요](#1-서비스-기능-전체-개요)
2. [전체 패키지 구조](#2-전체-패키지-구조)
3. [도메인 구성](#3-도메인-구성)
4. [각 도메인 내부 구조](#4-각-도메인-내부-구조)
5. [공통 컴포넌트 (common)](#5-공통-컴포넌트-common)
6. [인증 흐름 (OAuth2 + JWT)](#6-인증-흐름-oauth2--jwt)
7. [예외 처리 구조](#7-예외-처리-구조)
8. [API 엔드포인트 전체 목록](#8-api-엔드포인트-전체-목록)
9. [Enum / 값 목록](#9-enum--값-목록)
10. [DB 테이블 ↔ 엔티티 매핑](#10-db-테이블--엔티티-매핑)
11. [중요한 설계 결정 및 특이사항](#11-중요한-설계-결정-및-특이사항)
12. [AI 챗봇 설계안](#12-ai-챗봇-설계안)
13. [요청 처리 흐름](#13-요청-처리-흐름)
14. [인프라 및 실행 환경](#14-인프라-및-실행-환경)
15. [API 문서 (Swagger)](#15-api-문서-swagger)

---

## 1. 서비스 기능 전체 개요

Geharbang은 **제주 게스트하우스 스텝 구인/구직 플랫폼**이다.
게스트하우스 운영자는 숙소를 등록하고 스텝을 모집하고, 구직자는 지원서를 작성해서 공고에 지원한다.

### 사용자 유형

| 유형 | 조건 | 가능한 기능 |
|------|------|-------------|
| 비로그인 | - | 게스트하우스/공고 목록 조회, 추천 조회 |
| 로그인 (사용자) | 소셜 로그인 | 지원서 작성, 공고 지원, 찜하기, 내 정보 조회 |
| 사장님 | 인증서 `승인_완료` | 게스트하우스 등록, 공고 등록, 지원자 관리 (`isOwner: true`) |
| 시스템 운영자 | `User.role = 운영자` (DB 직접 설정) | 인증서 심사 승인/거부, 사장님 기능 사용 (`isAdmin: true`) |

---

### 기능별 요약

#### 소셜 로그인
- 카카오 / 구글 OAuth2 로그인
- 로그인 성공 시 JWT accessToken 발급
- 신규 유저는 자동으로 계정 생성

#### 내 정보 (프로필)
- 프로필 조회: 이름, 프로필 이미지, 운영자 여부, 인증서 심사 상태
- 지원서 작성 전: 기본 아이콘 + "지원서 작성 후 프로필 등록" 안내
- 지원서 작성 후: 지원서 이미지가 프로필 이미지로 표시

#### 지원서 (Application)
- 한 유저당 지원서 1개
- 작성 항목: 이름, 성별, 전화번호, 생년월일, 활동 가능 시작일, 가능 요일, 자기소개, MBTI, 스타일, 인스타그램 ID, 프로필 이미지
- `POST /api/v1/application` 하나로 등록/수정 모두 처리 (upsert)
  - 지원서가 없으면 새로 생성, 있으면 기존 내용 덮어씀
  - 이름·전화번호·생년월일·성별은 User 엔티티에, 나머지는 Application 엔티티에 저장
- 수정 플로우: `GET /api/v1/application/my` 로 기존 데이터를 불러와 폼을 채운 뒤 `POST /api/v1/application` 으로 전송
- 지원서 존재 여부 확인 API 별도 제공 (`/my/exist`)

#### 공고 지원 (Application Record)
- 특정 스텝 구인 공고에 지원서를 제출하는 행위
- 공고마다 추가 질문이 있을 수 있고, 지원 시 답변 포함
- 공고 작성자는 지원자 목록 조회 + 합격 처리 가능
- 지원자는 내 지원 내역 조회 가능 (합격 필터링, 페이지네이션)

#### 사장님 인증 (Certificate)
- 영업신고증 또는 관광사업등록증 파일 업로드 후 제출
- 관리자가 심사 → 승인/거부
- 승인 시 유저 Role이 운영자로 변경되지는 않음 (Certificate Status로 관리)
- 인증서 상태: 검토 대기 → 승인 완료 / 거부됨

#### 게스트하우스 게시글 (GuestHousePost)
- 인증 사장님 또는 시스템 운영자가 숙소 게시글 등록/수정/삭제
- 다양한 필터로 목록 조회: 지역, 가격, 파티 유형, 방 유형, 인원, 편의시설, 분위기
- 정렬: 최신순 / 조회순 / 찜 많은순
- 찜하기 가능
- 지역 추천 (랜덤)
- 게시글 상태: ACTIVE / INACTIVE

#### 스텝 구인 공고 (StaffRecruitment)
- 인증 사장님 또는 시스템 운영자가 공고 등록/수정/삭제
- 다양한 필터로 목록 조회: 지역, 근무 형태, 근무 요일, 휴무일, 기간, 스케줄, 성별
- 공고별 추가 질문 설정 가능
- 지역 추천 (랜덤)
- 찜하기 가능

#### 찜 (Wish)
- 스텝 구인 공고 찜 추가 / 삭제
- 게스트하우스 게시글 찜 추가 / 삭제
- 목록 조회의 `isWished`와 `찜_많은순` 정렬은 `wish` 테이블 기준으로 계산

#### 리뷰 (Review)
- 현재 구현은 로그인 사용자가 게스트하우스 게시글에 리뷰 작성/수정/삭제
- 스텝 공고 근무 경험 리뷰도 같은 `review` 도메인에서 작성/조회/요약 가능
- 게스트하우스 게시글 또는 스텝 공고당 사용자 1명은 활성 리뷰 1개만 작성 가능
- 별점, 본문, 리뷰 이미지 저장
- 삭제는 물리 삭제가 아니라 `Review.status = DELETED`로 처리
- 게스트하우스 상세 응답에 평균 별점, 리뷰 수, 내 리뷰 작성 여부 포함

#### 알림 (Notification)
- 로그인한 사용자의 인앱 알림 목록 조회
- 읽지 않은 알림 개수 조회
- 단일/전체 알림 읽음 처리
- Expo Push Token 등록/해제
- 사장님 인증 승인/거절, 스텝 공고 신규 지원, 지원 합격, 채팅 메시지 수신 시 알림 생성
- 인앱 알림 저장 후 Expo Push API로 OS 푸시 발송

#### 채팅 (Chat)
- 스텝 공고 지원 내역(`ApplicationRecord`), 스텝 공고(`StaffRecruitment`), 게스트하우스 게시글(`GuestHousePost`) 기준으로 사장님과 사용자 간 채팅방 생성
- 기존 메시지/채팅방 목록은 REST API로 조회
- 메시지 전송은 REST API로 저장하고, 저장 성공 후 WebSocket으로 채팅방 참여자에게 실시간 전달
- WebSocket 연결이 끊기거나 메시지를 놓쳐도 REST 메시지 조회로 복구 가능하도록 설계

#### 이미지 업로드
- 지원서 프로필 이미지 업로드 (1장)
- 게스트하우스/공고 이미지 다중 업로드
- 업로드된 파일은 서버 로컬 디렉토리에 저장 → nginx가 직접 서빙

---

## 2. 전체 패키지 구조

```
src/main/java/guesthouse/
│
├── common/                  # 공통 컴포넌트 (예외, 설정, 어노테이션)
│
├── oauth2/                  # 소셜 로그인 + JWT 인증
├── user/                    # 유저 정보
├── application/             # 스텝 지원서
├── application_record/      # 지원 내역 (어떤 공고에 지원했는지)
├── certificate/             # 사장님 인증서
├── guestHousePost/          # 게스트하우스 게시글
├── staffrecruitment/        # 스텝 구인 공고
├── review/                  # 게스트하우스 리뷰
├── wish/                    # 찜 목록
├── notification/            # 인앱 알림 + Expo Push
└── chat/                    # 1:1 채팅
```

도메인별로 패키지가 분리되어 있고, 각 도메인은 동일한 내부 구조를 가짐.

---

## 3. 도메인 구성

| 도메인 | 역할 |
|--------|------|
| `oauth2` | 카카오/구글 소셜 로그인, JWT 발급 |
| `user` | 유저 개인정보 조회/수정, 프로필, 운영자 권한 검증 |
| `application` | 스텝 지원서 작성/조회 |
| `application_record` | 특정 공고에 지원서를 제출하는 행위 기록 |
| `certificate` | 사장님 인증서 제출 및 관리자 심사 |
| `guestHousePost` | 게스트하우스 숙소 게시글 CRUD |
| `staffrecruitment` | 게스트하우스 스텝 구인 공고 CRUD |
| `review` | 게스트하우스 리뷰 작성/조회/수정/삭제 및 리뷰 요약 |
| `wish` | 스텝 구인 공고 / 게스트하우스 게시글 찜하기 기능 |
| `notification` | 사용자별 인앱 알림 조회/읽음 처리, Expo Push Token 관리 및 주요 이벤트 알림 생성 |
| `chat` | 스텝 공고/게스트하우스 기준 사장님과 사용자 간 1:1 채팅방/메시지 관리 |

---

## 4. 각 도메인 내부 구조

모든 도메인은 아래 구조를 동일하게 따름:

```
{domain}/
├── controller/     # HTTP 요청을 받아서 서비스 호출 후 응답 반환
├── service/        # 비즈니스 로직
├── repository/     # DB 접근 (JPA Repository)
├── domain/
│   ├── model/      # JPA 엔티티 (@Entity)
│   └── vo/         # 값 객체 (Enum, @Embeddable 등)
├── dto/
│   ├── request/    # 요청 DTO (클라이언트 → 서버)
│   └── response/   # 응답 DTO (서버 → 클라이언트)
├── mapper/         # 엔티티 ↔ DTO 변환 (일부 도메인)
└── exception/      # 도메인별 예외 코드 및 예외 클래스
```

**예시 — application 도메인:**
```
application/
├── controller/ApplicationController.java
├── service/ApplicationService.java
├── service/ImageService.java
├── repository/ApplicationRepository.java
├── domain/model/Application.java
├── domain/vo/DayOfWeek.java, Mbti.java, Style.java
├── dto/MyApplicationDTO.java
├── dto/request/ApplicationSaveRequest.java
├── dto/response/ApplicationExistResponse.java
├── mapper/ApplicationMapper.java
└── exception/ApplicationErrorCode.java, ApplicationException.java
```

---

## 5. 공통 컴포넌트 (common)

### 5-1. 예외 처리

```
common/exception/
├── GuestHouseException.java      # 모든 커스텀 예외의 부모 클래스
├── GlobalExceptionHandler.java   # @ControllerAdvice - 전역 예외 핸들러
└── message/
    ├── ErrorCode.java            # 예외 코드 인터페이스
    └── ValidationMessage.java    # 입력값 검증 메시지 상수
```

`ErrorCode` 인터페이스:
```java
public interface ErrorCode {
    String getMessage();   // 에러 메시지
    int getStatus();       // HTTP 상태 코드
    String getErrorCode(); // 에러 코드 문자열
}
```

도메인별 예외 코드가 이 인터페이스를 구현하고, `GuestHouseException`을 상속한 예외 클래스를 던지면 `GlobalExceptionHandler`가 통일된 형식으로 응답을 내려줌.

### 5-2. @UserId 어노테이션

컨트롤러 파라미터에 `@UserId`를 붙이면 JWT 토큰에서 userId를 자동으로 파싱해서 주입함.

```java
// 사용 예시
@GetMapping("/profile")
public ResponseEntity<?> getProfile(@UserId Long userId) {
    ...
}
```

**내부 동작 흐름:**
```
HTTP 요청 (Authorization: Bearer {token})
  → UserIdResolver.resolveArgument()
    → Authorization 헤더에서 토큰 추출
    → TokenProcessor.parseAccessToken(token) → userId 파싱
    → UserRepository.findById(userId) 존재 확인
    → userId 반환 → 컨트롤러 파라미터에 주입
```

`required = false`로 설정하면 비로그인 유저도 허용 (userId = null):
```java
@GetMapping("/posts")
public ResponseEntity<?> getPosts(@UserId(required = false) Long userId) {
    ...
}
```

### 5-3. 설정 파일

```
common/config/
├── QuerydslConfig.java    # JPAQueryFactory 빈 등록 (복잡한 쿼리용)
└── WebConfig.java         # UserIdResolver 등록
```

---

## 6. 인증 흐름 (OAuth2 + JWT)

### 전체 흐름

```
[앱] → 카카오/구글 로그인
  → [소셜 서버] → 인가 코드 발급
  → [앱] → /api/v1/oauth/{provider}/callback?code={인가코드}
  → [OAuth2Controller] → Oauth2Service.getAccessToken()
  → [소셜 서버] → 소셜 access token 발급
  → Oauth2Service.getUserInfo() → socialId 획득
  → AuthService.loginWithProvider(socialId, provider)
    → oauth2account 테이블에서 socialId 조회
      → 신규 유저: uuser 테이블에 User 생성 + oauth2account 저장
      → 기존 유저: userId 반환
  → TokenProcessor.generateAccessToken(userId)
  → [앱] → accessToken 저장
```

### 핵심 엔티티 관계

```
uuser (User)
  └── 1:1 → oauth2account (Oauth2Account)   # 소셜 계정 연동
  └── 1:1 → application (Application)       # 지원서
  └── 1:N → certificate (Certificate)       # 사장님 인증서
```

`oauth2account`에 `userId`를 직접 저장 (FK가 아닌 Long 타입):
```java
public class Oauth2Account {
    private Long userId;      // uuser.id 값을 그냥 저장
    private Provider provider; // KAKAO, GOOGLE
    private String socialId;   // 소셜 서비스의 고유 ID
}
```

### JWT 구조

알고리즘: **HS256** (HMAC-SHA256), 라이브러리: **JJWT**

```
Header: { alg: "HS256", type: "jwt" }
Payload: {
  userId: "1",         ← uuser.id (String으로 저장)
  sub: "AT",           ← Access Token 식별자 ("RT"이면 Refresh Token)
  iat: 발급시각,
  exp: 만료시각
}
Signature: HMAC-SHA256(header + payload, JWT_SECRET_KEY)
```

- **Access Token**: `sub = "AT"`, 만료시간 = `JWT_ACCESS_EXPIRATION_TIME`
- **Refresh Token**: `sub = "RT"`, 만료시간 = `JWT_REFRESH_EXPIRATION_TIME`
- `sub` 값으로 Access/Refresh 용도를 구분 — 잘못된 토큰 종류 사용 시 `INVALID_TOKEN_SUBJECT` 에러
- **Spring Security 없음** — 이 프로젝트는 Spring Security를 사용하지 않음. 인증은 전적으로 `UserIdResolver`가 처리

### 소셜 로그인 외부 API 호출

카카오/구글 서버와 통신 시 **Spring WebFlux의 WebClient** 사용 (RestTemplate 대신):

| 단계 | 카카오 | 구글 |
|------|--------|------|
| 로그인 URI 생성 | `kauth.kakao.com/oauth/authorize` | `accounts.google.com/o/oauth2/v2/auth` |
| 토큰 요청 | `kauth.kakao.com/oauth/token` | `oauth2.googleapis.com/token` |
| 유저 정보 | `kapi.kakao.com/v2/user/me` → `id` 필드 | `id_token` JWT 직접 파싱 → `sub` 필드 |

구글은 별도 유저 정보 API 호출 없이 `id_token`(JWT)을 Base64 디코딩해서 `sub` 값을 socialId로 사용.

---

## 7. 예외 처리 구조

### 계층 구조

```
RuntimeException
  └── GuestHouseException (common)
        ├── AuthenticationException (oauth2)
        ├── UserException (user)
        ├── ApplicationException (application)
        ├── CertificateException (certificate)
        └── ...
```

### 예외 발생 → 응답 흐름

```
Service에서 throw new ApplicationException(ApplicationErrorCode.NOT_FOUND)
  → GlobalExceptionHandler가 GuestHouseException 캐치
  → errorCode.getStatus() → HTTP 404
  → errorCode.getMessage() → "지원서를 찾을 수 없습니다."
  → JSON 응답 반환
```

`IllegalArgumentException` 같은 비커스텀 예외는 `GlobalExceptionHandler`가 못 잡아서 HTTP 500이 떨어짐.
**반드시 도메인 예외(`GuestHouseException` 상속)를 사용해야 함.**

---

## 8. API 엔드포인트 전체 목록

### 인증 (OAuth2)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| GET | `/api/v1/oauth/kakao/login` | 불필요 | 카카오 로그인 URI 반환 |
| GET | `/api/v1/oauth/kakao/callback` | 불필요 | 카카오 인가코드 처리 → `accessToken`을 응답 헤더에 담아 반환 |
| GET | `/api/v1/oauth/google/login` | 불필요 | 구글 로그인 URI 반환 |
| GET | `/api/v1/oauth/google/callback` | 불필요 | 구글 인가코드 처리 → `accessToken`을 응답 헤더에 담아 반환 |

### 유저 (User)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| GET | `/api/v1/user/profile` | 필요 | 내 프로필 조회 (이름, 이미지, `isOwner`, `inReview`, `isAdmin`, `certificateStatus`) |

### 지원서 (Application)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/api/v1/application` | 필요 | 지원서 등록/수정 (upsert — 지원서가 없으면 새로 생성, 있으면 기존 내용 덮어씀) |
| GET | `/api/v1/application/my` | 필요 | 내 지원서 조회 (User 개인정보 + Application 데이터를 합쳐서 반환 — 수정 폼 pre-fill용) |
| GET | `/api/v1/application/my/exist` | 필요 | 내 지원서 존재 여부 확인 |
| POST | `/api/v1/application/images` | 필요 | 지원서 프로필 이미지 업로드 (1장, multipart) |
| POST | `/api/v1/application/staff-recruitment/{recruitmentId}` | 필요 | 특정 공고에 지원서 제출 |

### 지원 내역 (Application Record)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| GET | `/api/v1/application-records/my` | 필요 | 내 지원 내역 조회 (`?onlyAccepted=true`, `?pageNumber=0`) |
| GET | `/api/v1/application-records/{recordId}` | 필요 | 특정 지원 내역 상세 조회 |
| GET | `/api/v1/application-records/questions/{recordId}` | 필요 | 지원 내역의 질문/답변 조회 |
| GET | `/api/v1/application-records/{id}/all` | 필요 | (공고 작성자) 특정 공고에 지원한 전체 지원자 목록 |
| POST | `/api/v1/application-records/{recordId}` | 필요 | (공고 작성자) 지원자 합격 처리 |

### 사장님 인증 (Certificate)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/api/v1/certificate/file-upload` | 필요 | 인증서 파일 업로드 (multipart, `fileType`, `fileName` 파라미터 포함) |
| POST | `/api/v1/certificate/owner` | 필요 | 인증서 제출 (심사 요청) |
| GET | `/api/v1/certificate` | 필요 | (관리자) 전체 제출된 인증서 목록 조회 |
| GET | `/api/v1/certificate/{certificateId}` | 필요 | (관리자) 인증서 상세 조회 |
| POST | `/api/v1/certificate/{certificateId}` | 필요 | (관리자) 인증서 승인/거부 결정 |

### 게스트하우스 게시글 (GuestHousePost)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| GET | `/api/v1/guest-houses` | 선택 | 게시글 목록 (필터/정렬, 로그인 시 `isWished` 포함) |
| GET | `/api/v1/guest-houses/recommendation` | 불필요 | 지역별 랜덤 추천 (`?region=제주시`) |
| GET | `/api/v1/guest-houses/{guestHousePostId}/details` | 선택 | 게시글 상세 조회 (로그인 시 `isWished` 포함) |
| POST | `/api/v1/guest-houses` | 필요 | 게스트하우스 게시글 등록 (승인_완료 사장님 또는 관리자 가능, 미충족 시 403) |
| GET | `/api/v1/guest-houses/owner` | 필요 | 내 게스트하우스 게시글 목록 |
| PUT | `/api/v1/guest-houses/{id}` | 필요 | 게시글 수정 (승인_완료 사장님 + 본인 게시글만 가능) |
| PATCH | `/api/v1/guest-houses/{id}` | 필요 | 게시글 상태 변경 (ACTIVE/INACTIVE) |
| DELETE | `/api/v1/guest-houses/{id}` | 필요 | 게시글 삭제 |

**조회 필터 (`GET /api/v1/guest-houses`):** `sort`, `region`, `keyword`, `lowestRoomPrice`, `highestRoomPrice`, `partyType`, `roomType`, `headCountType`, `amenities`, `moods`, `pageNumber`

목록 API는 `pageNumber`를 0부터 받으며 페이지당 10개씩 반환한다.
`sort=찜_많은순`은 `wish.guestHousePostId`별 wish count를 기준으로 정렬한다.
대표 이미지가 없는 게시글은 목록 응답에서 `imageUrl`을 빈 문자열로 내려준다.
목록과 상세 조회는 비회원 요청도 가능하지만, 로그인 토큰이 있으면 사용자별 `isWished`를 함께 내려준다.

### 스텝 구인 공고 (StaffRecruitment)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| GET | `/api/v1/staff-recruitment` | 선택 | 공고 목록 (필터/정렬, 로그인 시 `isWished` 포함) |
| GET | `/api/v1/staff-recruitment/recommendation` | 불필요 | 지역별 랜덤 추천 (`?region=제주시`) |
| GET | `/api/v1/staff-recruitment/{id}/details` | 선택 | 공고 상세 조회 (`weeklyWorkingDays`, 로그인 시 `isWished` 포함) |
| GET | `/api/v1/staff-recruitment/{id}/questions` | 필요 | 공고 지원 질문 조회 (지원 전 확인용) |
| POST | `/api/v1/staff-recruitment` | 필요 | 공고 등록 (승인_완료 사장님 또는 관리자 가능, 미충족 시 403) |
| GET | `/api/v1/staff-recruitment/owner` | 필요 | 내 공고 목록 |
| PUT | `/api/v1/staff-recruitment/{id}` | 필요 | 공고 수정 (승인_완료 사장님 + 본인 공고만 가능) |
| PATCH | `/api/v1/staff-recruitment/{id}` | 필요 | 공고 상태 변경 (ACTIVE/INACTIVE) |
| DELETE | `/api/v1/staff-recruitment/{id}` | 필요 | 공고 삭제 |

**조회 필터 (`GET /api/v1/staff-recruitment`):** `sort`, `keyword`, `region`, `workType`, `workDays`, `restDays`, `period`, `workScheduleType`, `gender`, `pageNumber`

목록 API는 `pageNumber`를 0부터 받으며 페이지당 10개씩 반환한다.
`sort=찜_많은순`은 `wish.staffRecruitmentId`별 wish count를 기준으로 정렬한다.
대표 이미지가 없는 공고는 목록 응답에서 `imageUrl`을 빈 문자열로 내려준다.
목록과 상세 조회는 비회원 요청도 가능하지만, 로그인 토큰이 있으면 사용자별 `isWished`를 함께 내려준다.

### 찜 (Wish)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| GET | `/api/v1/wish/staff-recruitment/my` | 필요 | 내가 찜한 스텝 구인 공고 목록 조회 |
| POST | `/api/v1/wish/staff-recruitment/{id}` | 필요 | 스텝 구인 공고 찜 추가 |
| DELETE | `/api/v1/wish/staff-recruitment/{id}` | 필요 | 스텝 구인 공고 찜 삭제 |
| GET | `/api/v1/wish/guest-houses/my` | 필요 | 내가 찜한 게스트하우스 게시글 목록 조회 |
| POST | `/api/v1/wish/guest-houses/{id}` | 필요 | 게스트하우스 게시글 찜 추가 |
| DELETE | `/api/v1/wish/guest-houses/{id}` | 필요 | 게스트하우스 게시글 찜 삭제 |

찜 추가 API는 `WishResponse`로 `wishId`를 반환한다. 이미 찜한 대상이면 새로 생성하지 않고 기존 `wishId`를 반환한다.
내가 찜한 목록 조회 API는 `pageNumber` query string을 받으며, 찜한 최신순으로 10개씩 반환한다.

### 리뷰 (Review)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/api/v1/guest-houses/{guestHousePostId}/reviews` | 필요 | 게스트하우스 리뷰 작성 |
| GET | `/api/v1/guest-houses/{guestHousePostId}/reviews` | 선택 | 게스트하우스 리뷰 목록 조회 (`?pageNumber=0`, 최신순 10개) |
| GET | `/api/v1/guest-houses/{guestHousePostId}/review-summary` | 선택 | 평균 별점, 리뷰 수, 내 리뷰 작성 여부 조회 |
| PUT | `/api/v1/reviews/{reviewId}` | 필요 | 내 리뷰 수정 |
| DELETE | `/api/v1/reviews/{reviewId}` | 필요 | 내 리뷰 삭제 (`DELETED` 상태 처리) |

리뷰 작성은 로그인 사용자만 가능하다.
같은 게스트하우스 게시글에 같은 사용자가 `ACTIVE` 리뷰를 2개 이상 작성할 수 없으며, 중복 작성 시 409 `DUPLICATED`를 반환한다.
별점은 1점 이상 5점 이하만 허용한다.
게스트하우스 상세 응답은 `averageRating`, `reviewCount`, `hasMyReview`를 포함한다.

스텝 공고/근무 경험 리뷰도 같은 리뷰 도메인에서 제공한다.

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/api/v1/staff-recruitment/{staffRecruitmentId}/reviews` | 필요 | 스텝 공고/근무 경험 리뷰 작성 |
| GET | `/api/v1/staff-recruitment/{staffRecruitmentId}/reviews` | 선택 | 스텝 공고 리뷰 목록 조회 (`?pageNumber=0`) |
| GET | `/api/v1/staff-recruitment/{staffRecruitmentId}/review-summary` | 선택 | 스텝 공고 평균 별점, 리뷰 수, 내 리뷰 작성 여부 조회 |

스텝 공고 리뷰 작성 권한은 단순 로그인만으로 열지 않고, 최소한 `application_record.staffRecruitmentId = staffRecruitmentId` 및 `application_record.userId = userId`가 존재하는 사용자로 제한하는 것을 기본안으로 한다.
더 엄격하게는 합격 처리된 지원자(`ApplicationRecord.status = 합격`)만 리뷰를 작성하게 할 수 있다.
스텝 공고 리뷰도 동일하게 대상 공고당 사용자 1명은 `ACTIVE` 리뷰 1개만 작성 가능해야 한다.

### 알림 (Notification)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| GET | `/api/v1/notifications` | 필요 | 내 알림 목록 조회 (`?pageNumber=0`, 최신순 10개) |
| GET | `/api/v1/notifications/unread-count` | 필요 | 읽지 않은 알림 개수 조회 |
| PATCH | `/api/v1/notifications/{id}/read` | 필요 | 내 특정 알림 읽음 처리 |
| PATCH | `/api/v1/notifications/read-all` | 필요 | 내 모든 알림 읽음 처리 |
| GET | `/api/v1/notification-settings` | 필요 | 내 알림 설정 조회 |
| PATCH | `/api/v1/notification-settings` | 필요 | 내 알림 설정 변경 |
| POST | `/api/v1/push-tokens` | 필요 | 내 Expo Push Token 저장/갱신 |
| DELETE | `/api/v1/push-tokens` | 필요 | 내 Expo Push Token 비활성화 |

알림은 `receiverId`가 로그인 사용자와 일치하는 데이터만 조회/수정할 수 있다.
현재 알림 생성 지점은 사장님 인증 승인/거절, 스텝 공고 신규 지원, 지원 합격 처리, 채팅 메시지 수신이다.
알림 생성 시 DB 인앱 알림을 먼저 저장하고, 활성화된 `push_token`이 있으면 Expo Push API로 푸시를 발송한다.
`pushEnabled=false`이면 OS 푸시만 발송하지 않고, `chatPushEnabled=false`이면 채팅 인앱 알림 생성과 채팅 푸시 발송을 모두 건너뛴다.
푸시 발송 실패는 핵심 비즈니스 트랜잭션을 실패시키지 않도록 로그만 남긴다.

### 채팅 (Chat)

채팅은 REST와 WebSocket을 함께 사용한다. REST는 저장/조회/권한 검증의 기준이 되고, WebSocket은 새 메시지 실시간 반영만 담당한다.

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/api/v1/chats/rooms` | 필요 | 지원 내역/스텝 공고/게스트하우스 기준 채팅방 생성 또는 기존 방 반환 |
| GET | `/api/v1/chats/rooms` | 필요 | 내가 참여 중인 채팅방 목록 조회 |
| GET | `/api/v1/chats/rooms/{roomId}/messages` | 필요 | 채팅방 메시지 조회 (`?pageNumber=0`, 최신순 30개) |
| POST | `/api/v1/chats/rooms/{roomId}/messages` | 필요 | 메시지 저장 후 WebSocket으로 실시간 전달 |
| PATCH | `/api/v1/chats/rooms/{roomId}/read` | 필요 | 채팅방의 상대 메시지 읽음 처리 |

채팅방 생성 요청은 `applicationRecordId`, `staffRecruitmentId`, `guestHousePostId` 중 하나만 받는다.
`applicationRecordId` 기준 채팅방은 공고 작성자(`StaffRecruitment.ownerId`)와 지원자(`ApplicationRecord.userId`) 사이에 생성한다.
`staffRecruitmentId`, `guestHousePostId` 기준 채팅방은 게시글 작성자와 현재 로그인 사용자 사이에 생성하며, 본인 게시글에 대해서는 채팅방을 생성하지 않는다.
채팅방 조회/전송/읽음 처리는 `ownerId == userId || applicantId == userId`인 참여자만 가능하다.
WebSocket 연결은 JWT access token을 handshake 시 전달하고, 서버는 `TokenProcessor.parseAccessToken()`으로 사용자 ID를 검증한다.
WebSocket 엔드포인트는 `/ws/chats?token={accessToken}&roomId={roomId}`이며, 클라이언트가 직접 WebSocket으로 메시지를 보내지는 않는다.
`roomId`는 상대방이 현재 같은 채팅방을 보고 있는지 판단하는 presence 용도다.
상대방이 같은 채팅방에 접속 중이면 채팅 알림을 생성하지 않고, 접속 중이 아니어도 같은 채팅방/수신자 조합은 30초에 한 번만 알림을 생성한다.

### 좌표 규칙

게스트하우스 게시글과 스텝 공고의 `location.coordinates`는 요청과 응답 모두 `[경도, 위도]` 순서로 통일한다.
JTS `Point`에는 `x=경도`, `y=위도`로 저장하고, 상세 조회 응답도 `[point.getX(), point.getY()]`로 내려준다.

### 지도 (Map)

지도 탭은 게스트하우스와 스텝 공고를 제주 지도 위에서 한 번에 탐색하는 화면으로 확장한다.
현재 게스트하우스 게시글과 스텝 공고 모두 좌표를 저장하고 있으므로, 초기 지도 MVP는 별도 지도 DB 없이 기존 `guest_house_post`, `staff_recruitment`의 좌표를 사용한다.

권장 API:

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| GET | `/api/v1/maps/guest-houses` | 선택 | 지도 영역 안의 게스트하우스 마커 조회 |
| GET | `/api/v1/maps/staff-recruitments` | 선택 | 지도 영역 안의 스텝 공고 마커 조회 |
| GET | `/api/v1/maps/markers` | 선택 | 게스트하우스/스텝 공고 통합 마커 조회 |

요청 파라미터:

| 파라미터 | 설명 |
|----------|------|
| `southWestLat`, `southWestLng` | 현재 지도 화면의 남서쪽 좌표 |
| `northEastLat`, `northEastLng` | 현재 지도 화면의 북동쪽 좌표 |
| `targetType` | `GUEST_HOUSE_POST`, `STAFF_RECRUITMENT`, `ALL` |
| `region` | 선택 지역 필터 |
| `keyword` | 게스트하우스명, 공고명, 소개글 검색 |

응답 예시:

```json
{
  "markers": [
    {
      "type": "GUEST_HOUSE_POST",
      "targetId": 12,
      "title": "제주 달빛 게하",
      "latitude": 33.4996,
      "longitude": 126.5312,
      "imageUrl": "/images/...",
      "summary": "조용한 분위기, 리뷰 4.8점"
    },
    {
      "type": "STAFF_RECRUITMENT",
      "targetId": 31,
      "title": "카페 스텝 모집",
      "latitude": 33.5101,
      "longitude": 126.4914,
      "imageUrl": "/images/...",
      "summary": "단기 가능, 숙식 제공"
    }
  ]
}
```

지도 검색 기준:

- `status = ACTIVE`인 게스트하우스/스텝 공고만 노출한다.
- 좌표가 없는 게시글/공고는 지도 마커에서 제외한다.
- 지도 영역 bounds 안에 있는 데이터만 반환해 마커 수를 제한한다.
- 통합 마커 API는 FE 지도 탭 MVP에 적합하고, 이후 성능이 필요하면 게스트하우스/스텝 공고 API를 분리 호출한다.
- 마커 클릭 시 FE는 상세 데이터를 모두 다시 받지 않고, 마커 응답의 최소 정보로 바텀시트를 먼저 보여준 뒤 상세 화면으로 이동한다.

### 이미지 (Image)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/api/v1/application/images` | 필요 | 지원서 프로필 이미지 업로드 (1장, `image` 파라미터) |
| POST | `/api/v1/images` | 필요 | 게스트하우스/공고 이미지 다중 업로드 (`images` 파라미터, URL 배열 반환) |

---

## 9. Enum / 값 목록

### 지역 (Region)
`제주시` / `서귀포시` / `서부권` / `동부권` / `도서지역` / `중문_대정`

### 정렬 (SortType)
`최신순` / `조회순` / `찜_많은순`

### 요일 (DayOfWeek)
`MONDAY(월)` / `TUESDAY(화)` / `WEDNESDAY(수)` / `THURSDAY(목)` / `FRIDAY(금)` / `SATURDAY(토)` / `SUNDAY(일)`

### MBTI
`ISTJ` / `ISFJ` / `INFJ` / `INTJ` / `ISTP` / `ISFP` / `INFP` / `INTP` /
`ESTP` / `ESFP` / `ENFP` / `ENTP` / `ESTJ` / `ESFJ` / `ENFJ` / `ENTJ`

### 스타일 (Style)
`FRIENDLY(친근한)` / `ACTIVE(활발한)` / `CALM(차분한)` / `DILIGENT(성실한)` / `HUMOROUS(유머)` / `RESPONSIBLE(책임감)`

### 게스트하우스 분위기 (Mood)
`조용한` / `사교적` / `힐링` / `사색` / `활발한` / `잔잔한` / `감성` / `휴식`

### 파티 유형 (PartyType)
`술파티` / `포틀럭` / `디너_파티` / `클럽_파티` / `기타`

### 방 유형 (RoomType)
`여성전용` / `남성전용`

### 인원 (RoomHeadCount)
`_1인실` / `_2인실` / `_3인이상`

### 근무 스케줄 (WorkScheduleType)
`주1일` / `주2일` / `주3일` / `주4일` / `주5일`

### 근무 형태 (WorkType)
`로테이션` / `_7일_기준`

### 근무 기간 (WorkingPeriod)
`단기` / `중기` / `장기`

### 성별 (Gender)
`남` / `여` / `무관` (구인공고) / `MALE` / `FEMALE` (유저)

### 인증서 유형 (CertificateType)
`영업신고증` / `관광사업등록증`

### 인증서 상태 (Certificate Status)
`검토_대기` → `승인_완료` / `거부됨`

### 지원 내역 상태 (ApplicationRecord Status)
`대기중` → `합격`

### 리뷰 상태 (ReviewStatus)
`ACTIVE` / `HIDDEN` / `DELETED`

### 유저 역할 (Role)
`사용자` / `운영자`

---

## 10. DB 테이블 ↔ 엔티티 매핑

| 테이블 | 엔티티 | 비고 |
|--------|--------|------|
| `uuser` | `User` | 테이블명 충돌 방지용 이름 (`@Table(name = "uuser")`) |
| `oauth2account` | `Oauth2Account` | |
| `application` | `Application` | |
| `available_day_of_week` | `Application.availableDayOfWeek` | `@ElementCollection` |
| `style` | `Application.style` | `@ElementCollection` |
| `application_record` | `ApplicationRecord` | |
| `certificate` | `Certificate` | |
| `guest_house_post` | `GuestHousePost` | |
| `guest_house_post_image` | `GuestHousePostImage` | 게스트하우스 대표/본문 이미지 URL과 순서 저장 |
| `amenity` | `Amenity` | 게스트하우스 편의시설, `guestHousePostId`로 게시글 참조 |
| `room` | `Room` | 객실명, 방 유형, 인원, 체크인/아웃, 가격 |
| `room_image` | `RoomImage` | 객실 이미지 URL과 순서 저장 |
| `party` | `Party` | 파티 유형, 시간, 장소, 비용, 외부 게스트 허용 여부 |
| `party_image` | `PartyImage` | 파티 이미지 URL과 순서 저장 |
| `mood` | `GuestHousePost.moods` | `@ElementCollection`, 게스트하우스 분위기 |
| `weekly_day` | `Party.weeklyDays` | `@ElementCollection`, 파티 진행 요일 |
| `staff_recruitment` | `StaffRecruitment` | |
| `staff_recruitment_job` | `StaffRecruitmentJob` | 직무명, 근무 시간, 근무/휴무 일수, 스케줄 |
| `staff_recruitment_image` | `StaffRecruitmentImage` | 대표/내용 이미지 URL과 순서 저장 |
| `staff_recruitment_question` | `StaffRecruitmentQuestion` | 공고별 추가 질문 |
| `review` | `Review` | 게스트하우스/스텝 공고 리뷰, `targetType`, `guestHousePostId`, `staffRecruitmentId`, `userId`, `rating`, `status` 저장 |
| `review_image` | `ReviewImage` | 리뷰 이미지 URL과 순서 저장 |
| `wish` | `Wish` | `staffRecruitmentId` 또는 `guestHousePostId` 중 하나로 찜 대상을 구분 |
| `notification` | `Notification` | `receiverId` 기준으로 사용자별 알림을 저장하고 `isRead`로 읽음 여부 관리 |
| `push_token` | `PushToken` | 사용자별 Expo Push Token 저장, 로그아웃 시 `isActive=false` 처리 |
| `chat_room` | `ChatRoom` | 지원 내역/스텝 공고/게스트하우스 기준으로 사장님과 사용자 간 1:1 방 관리 |
| `chat_message` | `ChatMessage` | 채팅방별 메시지 저장, `senderId`와 `isRead`로 발신자/읽음 여부 관리 |

### User 엔티티 특이사항

```java
@Table(name = "uuser")          // user는 MySQL 예약어라 uuser 사용
@Embedded PersonalInfo          // name, phoneNumber, birthDate, gender를 묶은 값 객체
@Enumerated(EnumType.STRING)
Role role;                      // "사용자" 또는 "운영자" (한글 Enum)
```

`PersonalInfo`는 `@Embeddable`로 별도 테이블 없이 uuser 테이블 컬럼으로 저장됨.

### 리뷰 대상 확장 방향

`Review` 엔티티는 별도 `StaffRecruitmentReview` 테이블을 만들지 않고, 리뷰 정책/이미지/상태/요약 로직을 공유할 수 있도록 다형 대상 구조를 사용한다.

권장 컬럼:

| 컬럼 | 설명 |
|------|------|
| `target_type` | `GUEST_HOUSE_POST` 또는 `STAFF_RECRUITMENT` |
| `guest_house_post_id` | 게스트하우스 리뷰일 때만 값 존재 |
| `staff_recruitment_id` | 스텝 공고 리뷰일 때만 값 존재 |
| `user_id` | 리뷰 작성자 |
| `rating` | 1~5 별점 |
| `content` | 리뷰 본문 |
| `status` | `ACTIVE`, `HIDDEN`, `DELETED` |

`guestHousePostId`, `staffRecruitmentId`는 `targetType`에 맞는 대상 ID 하나만 존재하도록 애플리케이션 레벨에서 검증한다.
DB 제약까지 강제하려면 MySQL `CHECK` 제약 또는 마이그레이션 스크립트가 필요하지만, 현재 프로젝트는 Hibernate `ddl-auto: update`를 사용하므로 초기에는 서비스 검증으로 막는 편이 안전하다.

중복 작성 방지 기준:

| 대상 | 기준 |
|------|------|
| 게스트하우스 리뷰 | `targetType = GUEST_HOUSE_POST`, `guestHousePostId`, `userId`, `status = ACTIVE` |
| 스텝 공고 리뷰 | `targetType = STAFF_RECRUITMENT`, `staffRecruitmentId`, `userId`, `status = ACTIVE` |

스텝 공고 리뷰 작성 권한:
1. 기본안: 해당 공고에 지원한 기록이 있는 사용자만 작성 가능
2. 권장안: 해당 공고에 지원했고 합격 처리된 사용자만 작성 가능
3. 추후 근무 완료 상태가 생기면 `ApplicationRecord.status = 근무완료` 같은 상태를 추가해 실제 근무 완료자만 작성 가능하게 변경

---

## 11. 중요한 설계 결정 및 특이사항

### 지원 시 스냅샷 저장

공고에 지원할 때 **지원서를 JSON으로 직렬화**해서 `application_record.applicationSnapShot` 컬럼에 저장함.
또한 지원 당시 공고 요약 정보도 함께 저장한다.

```
지원 시점 지원서 → JSON 문자열로 직렬화 → DB 저장
이후 지원서를 수정해도 지원 당시 내용은 그대로 보존
공고 작성자가 지원자 정보를 조회할 때 스냅샷을 역직렬화해서 반환

지원 시점 공고 제목/지역/대표 이미지 → application_record에 저장
이후 공고 제목, 지역, 대표 이미지가 수정되어도 내 지원 내역은 지원 당시 공고 정보로 표시
```

이 설계 덕분에 지원자가 지원서를 수정해도 공고 작성자는 지원 당시 내용을 볼 수 있고, 공고가 수정되어도 지원자의 내 지원 내역에는 지원 당시 공고 요약 정보가 유지됨.
기존 지원 기록처럼 공고 스냅샷 컬럼이 비어 있는 데이터는 현재 공고 데이터를 조회하는 fallback을 사용한다.

---

### 사장님 인증 ↔ Role 변경이 분리됨

`Certificate.decide(isApproved)` 는 `Certificate.status`만 변경하고 `User.role`은 변경하지 않음.

즉, 인증서가 승인돼도 Role은 자동으로 `운영자`로 바뀌지 않음.
관리자 기능(인증서 목록, 인증서 승인/거부)은 `userService.validateAdmin(userId)`로 Role을 직접 확인하는 방식으로 보호됨.

프로필에서 사장님/운영진 여부는 `GET /api/v1/user/profile` 응답 필드로 FE에 전달됨.

**FE 권한 분기용 필드 정리:**

| 필드 | 기준 | 의미 |
|------|------|------|
| `isOwner` | 인증서 `승인_완료` 상태 (`existsByUserIdAndStatus`) | 인증 사장님 배지, 사장님 권한 UI |
| `inReview` | 인증서 `검토_대기` 상태 | 심사 진행 중 배지 표시 |
| `isAdmin` | `User.role == Role.운영자` | 관리자 배지, 인증서 심사 승인/거부, 사장님 기능 사용 가능 |
| `certificateStatus` | 최근 인증서 status 문자열 (`null` \| `검토_대기` \| `승인_완료` \| `거부됨`) | 거절 상태 등 세부 UX 분기 |

`isAdmin`은 DB에서 직접 `role = 운영자`로 설정된 경우에만 `true`. 인증서 승인으로는 변경되지 않음.
운영자 지정 API는 없으며, 현재는 DB 직접 수정으로만 가능.

---

### 사장님 전용 API 보호

게스트하우스/공고 **등록 및 수정**은 `승인_완료` 인증서 보유자 또는 시스템 운영자만 가능. `UserService.validateOwnerStatus()`로 일괄 검증.
수정/상태 변경/삭제처럼 특정 게시글을 대상으로 하는 API는 추가로 `existsByOwnerIdAndId()`로 본인 글인지 확인한다.

```java
// UserService.validateOwnerStatus()
User user = findById(userId);
boolean isApprovedOwner = certificateRepository.existsByUserIdAndStatus(userId, Status.승인_완료);
if (!user.isAdmin() && !isApprovedOwner)
    throw new UserException(UserErrorCode.NOT_APPROVED_OWNER);  // 403
```

| API | 권한 체크 | 미인증 시 응답 |
|-----|----------|--------------|
| `POST /api/v1/guest-houses` | `validateOwnerStatus()` (`isOwner` 또는 `isAdmin`) | 403 `NOT_APPROVED_OWNER` |
| `POST /api/v1/staff-recruitment` | `validateOwnerStatus()` (`isOwner` 또는 `isAdmin`) | 403 `NOT_APPROVED_OWNER` |
| `PUT /api/v1/guest-houses/{id}` | `validateOwnerStatus()` + `existsByOwnerIdAndId()` | 403 또는 404 |
| `PUT /api/v1/staff-recruitment/{id}` | `validateOwnerStatus()` + `existsByOwnerIdAndId()` | 403 또는 404 |
| `PATCH /api/v1/guest-houses/{id}` | `existsByOwnerIdAndId()` (본인 글 확인) | 기존 에러 |
| `DELETE /api/v1/guest-houses/{id}` | `existsByOwnerIdAndId()` (본인 글 확인) | 기존 에러 |

---

### 게시글/공고 수정 방식

게스트하우스 게시글과 스텝 구인 공고 수정 API는 **전체 교체형 PUT**으로 동작한다.

| 도메인 | 기본 엔티티 | 하위 목록 처리 |
|--------|------------|----------------|
| GuestHousePost | `GuestHousePost.update()`로 기본 정보 변경 | 이미지, 편의시설, 파티, 객실 및 각 이미지 삭제 후 재저장 |
| StaffRecruitment | `StaffRecruitment.update()`로 기본 정보 변경 | 직무, 이미지, 추가 질문 삭제 후 재저장 |

하위 목록은 항목별 diff/update를 하지 않고 요청 body 기준으로 다시 구성한다.
따라서 프론트는 수정 요청 시 기존에 유지할 이미지/파티/객실/질문도 모두 포함해서 보내야 한다.

---

### 중복 지원 방지

공고에 중복 지원 시 `existsByStaffRecruitmentIdAndUserId()` 로 체크 후 `ApplicationRecordException(DUPLICATED_APPLICATION)`을 발생시킨다.
`GlobalExceptionHandler`가 `GuestHouseException` 계층을 공통 에러 응답으로 변환하므로, 중복 지원은 500이 아니라 400 도메인 에러로 내려간다.

---

### TimeEntity — 자동 시간 기록

거의 모든 엔티티가 `TimeEntity`를 상속하며, `createdAt` / `updatedAt` 이 자동으로 기록됨.

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class TimeEntity {
    @CreatedDate LocalDateTime createdAt;
    @LastModifiedDate LocalDateTime updatedAt;
}
```

---

### 파일 저장 방식

```
업로드된 파일
  → 서버 로컬 디렉토리에 저장 (IMAGE_DIRECTORY_PATH, CERTIFICATE_DIRECTORY_PATH)
  → DB에는 URL 경로만 저장 (/images/application/{fileName}, /files/certifications/{fileName})
  → 클라이언트가 이미지 요청 시 Spring Boot가 아닌 nginx가 직접 파일 서빙
```

인증서 파일명에는 UUID 앞 8자리를 prefix로 붙여 충돌 방지:
```
UUID(8자리) + 원본파일명 → /files/certifications/a3f2b1c4originalName.pdf
```

허용 파일 형식: `jpeg`, `jpg`, `pdf`, `png`

---

## 12. AI 챗봇 설계안

> 이 섹션은 아직 구현된 기능이 아니라, FE의 `app/(tabs)/ai.tsx` AI 탭을 실제 통합 AI 챗봇으로 확장하기 위한 설계안이다.

AI 챗봇은 기존 `chat` 도메인과 분리한다.
현재 `chat` 도메인은 사장님과 사용자 간 1:1 메시지 저장/전달 시스템이고, AI 챗봇은 게하르방 서비스 안내, 제주 여행 상담, 게스트하우스 추천, 스텝 공고 추천을 통합 처리하는 별도 도메인이다.

### 목표

AI 탭에서 사용자가 자연어로 질문하면 다음 질문 유형을 한 화면에서 처리한다.

| 질문 유형 | 예시 | 주된 근거 |
|-----------|------|-----------|
| 게하르방 서비스 안내 | "스텝 지원은 어떻게 해?", "사장님 인증은 뭐야?" | 서비스 가이드/FAQ RAG 문서 |
| 제주 관광/여행 상담 | "비 오는 날 갈 만한 곳 추천해줘", "성산 근처 반나절 코스 짜줘" | 제주 관광 RAG 문서 + LLM 일반 지식 |
| 게스트하우스 추천 | "제주시에서 조용하고 후기 좋은 게하 추천해줘" | MySQL 게스트하우스/객실/파티/편의시설/리뷰 데이터 |
| 스텝 공고 추천 | "2주 정도 일할 수 있고 숙식 제공되는 스텝 공고 찾아줘" | MySQL 스텝 공고/직무/혜택/질문 데이터, 향후 스텝 공고 리뷰 |
| 혼합 질문 | "비 오는 날 코스랑 근처 조용한 게하 추천해줘" | 관광 RAG + 게스트하우스 DB 검색 |

### 설계 원칙

- LLM이 직접 SQL을 만들거나 DB 전체를 자유롭게 조회하지 않는다.
- 백엔드가 검색 도구를 통제하고, LLM은 질문 분석과 최종 답변 생성을 담당한다.
- 게스트하우스/스텝 공고 추천은 반드시 실제 DB 후보 안에서만 말한다.
- DB에 없는 게스트하우스나 스텝 공고를 있는 것처럼 생성하지 않는다.
- 영업시간, 입장료, 교통 정보처럼 바뀔 수 있는 제주 관광 정보는 "확인 필요" 문구를 함께 둔다.
- 답변은 텍스트뿐 아니라 FE가 카드로 렌더링할 수 있는 `cards`를 함께 반환한다.

### 권장 패키지 구조

```
ai/
├── controller/
│   └── AiChatController.java
├── service/
│   ├── AiConversationService.java       # 대화 저장, 전체 orchestration
│   ├── AiIntentRouter.java              # 질문 의도 분류
│   ├── GuestHouseAiSearchService.java   # 게스트하우스 DB 검색 도구
│   ├── StaffRecruitmentAiSearchService.java # 스텝 공고 DB 검색 도구
│   ├── ServiceGuideRetriever.java       # 서비스 안내 RAG
│   ├── JejuTravelRetriever.java         # 제주 관광 RAG
│   └── LlmAnswerService.java            # LLM API 호출 및 최종 답변 생성
├── domain/
│   ├── model/
│   │   ├── AiConversation.java
│   │   ├── AiMessage.java
│   │   ├── AiMessageCard.java
│   │   └── AiRetrievalLog.java
│   └── vo/
│       ├── AiIntent.java
│       ├── AiMessageRole.java
│       ├── AiCardType.java
│       └── AiSourceType.java
├── dto/
│   ├── request/
│   │   └── AiChatRequest.java
│   └── response/
│       ├── AiChatResponse.java
│       └── AiChatCardResponse.java
└── repository/
    ├── AiConversationRepository.java
    ├── AiMessageRepository.java
    ├── AiMessageCardRepository.java
    └── AiRetrievalLogRepository.java
```

### 권장 API

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/api/v1/ai/chat` | 선택 | AI 챗봇에 메시지를 보내고 답변/카드 응답 |
| GET | `/api/v1/ai/conversations` | 필요 | 내 AI 대화 목록 조회 |
| GET | `/api/v1/ai/conversations/{conversationId}/messages` | 필요 | 특정 AI 대화 메시지 조회 |
| DELETE | `/api/v1/ai/conversations/{conversationId}` | 필요 | 내 AI 대화 삭제 |

초기 MVP는 `POST /api/v1/ai/chat`만 구현해도 된다.
비로그인 사용자는 `conversationId = null`로 1회성 답변을 받을 수 있고, 로그인 사용자는 대화 이력을 저장할 수 있다.

요청 예시:
```json
{
  "conversationId": null,
  "message": "비 오는 날 제주에서 갈 만한 곳이랑 근처 조용한 게하 추천해줘"
}
```

응답 예시:
```json
{
  "conversationId": 1,
  "messageId": 10,
  "answer": "비 오는 날에는 실내 동선이 좋은 관광지를 먼저 잡고, 숙소는 조용한 분위기와 리뷰 평점이 높은 곳을 함께 보는 것이 좋아요.",
  "cards": [
    {
      "type": "TRAVEL_SPOT",
      "targetId": null,
      "title": "아르떼뮤지엄 제주",
      "description": "비 오는 날에도 이동 부담이 적은 실내 관광지예요.",
      "imageUrl": ""
    },
    {
      "type": "GUEST_HOUSE",
      "targetId": 12,
      "title": "제주 달빛 게하",
      "description": "조용한 분위기, 평균 평점 4.8점, 리뷰 17개",
      "imageUrl": "/images/example.jpg"
    }
  ],
  "suggestedQuestions": [
    "뚜벅이로 갈 수 있는 코스로 짜줘",
    "조용한 게스트하우스만 더 보여줘"
  ]
}
```

### 의도 분류

`AiIntentRouter`는 사용자 질문을 아래 의도 중 하나 이상으로 분류한다.
초기에는 LLM 구조화 출력으로 분류하고, 비용/속도가 문제가 되면 지역/가격/기간 등 일부 필드는 규칙 기반 전처리를 섞는다.

| Intent | 설명 |
|--------|------|
| `SERVICE_GUIDE` | 게하르방 서비스 사용법, 정책, 화면 안내 |
| `JEJU_TRAVEL` | 제주 관광지, 코스, 여행 팁 |
| `GUEST_HOUSE_RECOMMENDATION` | 게스트하우스 추천/검색 |
| `STAFF_RECRUITMENT_RECOMMENDATION` | 스텝 공고 추천/검색 |
| `MIXED_TRAVEL_AND_GUEST_HOUSE` | 관광 + 숙소 추천 혼합 |
| `MIXED_GUEST_HOUSE_AND_STAFF` | 게스트하우스 정보 + 스텝 공고 혼합 |
| `GENERAL_CHAT` | 서비스와 직접 관련 없는 일반 대화 |

분류 결과 예시:
```json
{
  "intent": "STAFF_RECRUITMENT_RECOMMENDATION",
  "region": ["제주시"],
  "workingPeriods": ["단기"],
  "workScheduleTypes": ["주5일"],
  "gender": "무관",
  "keywords": ["숙식", "카페"],
  "needsCards": true
}
```

### 현재 DB 구조 기반 검색 도구

#### 게스트하우스 검색 도구

`GuestHouseAiSearchService`는 현재 DB 구조에서 아래 데이터를 사용한다.

| 목적 | 테이블/엔티티 | 사용 필드 |
|------|---------------|-----------|
| 기본 후보 | `guest_house_post` / `GuestHousePost` | `id`, `guestHouseName`, `region`, `lotNumberAddress`, `roadNameAddress`, `coordinates`, `introduction`, `moods`, `status` |
| 대표 이미지 | `guest_house_post_image` / `GuestHousePostImage` | `guestHousePostId`, `imageUrl`, `index` |
| 가격/객실 조건 | `room` / `Room` | `type`, `headCount`, `pricePerNight`, `checkInTime`, `checkOutTime` |
| 파티/조용함 판단 | `party` / `Party` | `partyType`, `startTime`, `endTime`, `moods`, `isExternalGuestAllowed`, `information` |
| 편의시설 | `amenity` / `Amenity` | `value` |
| 리뷰 품질 | `review` / `Review` | `rating`, `content`, `status = ACTIVE` |
| 찜 인기도 | `wish` / `Wish` | `guestHousePostId` count |

AI용 후보 DTO는 LLM에 너무 많은 원문을 넘기지 않도록 압축한다.

```json
{
  "id": 12,
  "name": "제주 달빛 게하",
  "region": "제주시",
  "address": "제주시 ...",
  "moods": ["조용한", "휴식"],
  "minRoomPrice": 30000,
  "amenities": ["와이파이", "세탁기"],
  "partyTypes": [],
  "averageRating": 4.8,
  "reviewCount": 17,
  "reviewSnippets": ["혼자 쉬기 좋았어요", "밤에 조용했어요"],
  "imageUrl": "/images/..."
}
```

검색 우선순위:
1. `status = ACTIVE`만 후보로 사용
2. 지역/가격/객실/편의시설/분위기처럼 명확한 조건은 QueryDSL 조건으로 필터링
3. 평점/리뷰 수/찜 수는 정렬 또는 tie-breaker로 사용
4. "조용한", "혼자 쉬기 좋은" 같은 표현은 `moods`, `party` 존재 여부, 리뷰 snippet을 함께 본다

#### 스텝 공고 검색 도구

`StaffRecruitmentAiSearchService`는 현재 DB 구조에서 아래 데이터를 사용한다.

| 목적 | 테이블/엔티티 | 사용 필드 |
|------|---------------|-----------|
| 기본 후보 | `staff_recruitment` / `StaffRecruitment` | `id`, `title`, `guestHouseName`, `region`, `lotNumberAddress`, `roadNameAddress`, `coordinates`, `startDate`, `workingPeriod`, `content`, `ownerMessage`, `status`, `viewCount` |
| 성별/혜택/장점 | `Feature` embedded | `gender`, `advantages`, `employeeBenefits` |
| 연락처 | `Contact` embedded | `instagramId`, `phoneNumber`, `webSite` |
| 직무/근무 조건 | `staff_recruitment_job` / `StaffRecruitmentJob` | `name`, `startTime`, `endTime`, `job`, `standard`, `workDays`, `restDays`, `workScheduleType` |
| 대표/내용 이미지 | `staff_recruitment_image` / `StaffRecruitmentImage` | `type`, `imageUrl`, `index` |
| 추가 질문 부담 | `staff_recruitment_question` / `StaffRecruitmentQuestion` | question count, `content` |
| 근무 경험 리뷰 | `review` / `Review` | 향후 `targetType = STAFF_RECRUITMENT`, `staffRecruitmentId`, `rating`, `content`, `status = ACTIVE` |
| 인기도 | `wish` / `Wish` | `staffRecruitmentId` count |

AI용 후보 DTO 예시:
```json
{
  "id": 31,
  "title": "제주 감성 게하 스텝 모집",
  "guestHouseName": "제주 감성 게하",
  "region": "제주시",
  "workingPeriod": "단기",
  "startDate": "2026-07-15",
  "gender": "무관",
  "jobs": [
    {
      "name": "카페 보조",
      "workDays": 5,
      "restDays": 2,
      "startTime": "09:00",
      "endTime": "15:00"
    }
  ],
  "advantages": "바다 근처, 초보 가능",
  "employeeBenefits": "숙식 제공",
  "questionCount": 2,
  "averageRating": 4.6,
  "reviewCount": 8,
  "reviewSnippets": ["숙소가 깔끔했고 업무 설명이 친절했어요"],
  "imageUrl": "/images/..."
}
```

검색 우선순위:
1. `status = ACTIVE`만 후보로 사용
2. 지역, 근무 기간, 성별, 근무/휴무 일수, 스케줄은 QueryDSL 조건으로 필터링
3. "숙식 제공", "카페 업무", "바다 근처" 같은 자연어 조건은 `content`, `advantages`, `employeeBenefits`, `job` 문자열 검색으로 1차 대응
4. "지원 질문 적은 공고"는 `staff_recruitment_question` 개수를 정렬 조건으로 사용할 수 있다
5. 스텝 공고 리뷰가 추가되면 평균 별점, 리뷰 수, 리뷰 snippet을 신뢰도/근무 만족도 근거로 함께 사용한다

### RAG 지식베이스 분리

DB 검색과 문서 RAG는 분리한다.
게스트하우스/스텝 공고는 최신 DB 상태가 중요하므로 SQL 기반 검색 도구를 우선 사용하고, 서비스 안내/관광 정보는 문서 RAG를 사용한다.

| 지식베이스 | 내용 | 저장/검색 방식 |
|------------|------|----------------|
| `service_guide` | 게하르방 이용 방법, 사장님 인증, 지원서 작성, 스텝 지원, 찜, 채팅, 리뷰 정책 | Markdown/DB 문서 + vector search |
| `jeju_travel` | 제주 관광지, 지역별 코스, 비 오는 날/뚜벅이/혼자 여행 팁 | Markdown/DB 문서 + vector search |
| `guesthouse_dynamic` | 게스트하우스 소개글, 리뷰 요약, 객실/파티/편의시설 | MySQL 검색 도구 우선, 추후 embedding 보조 |
| `staff_recruitment_dynamic` | 스텝 공고 본문, 근무 조건, 혜택, 추가 질문, 향후 스텝 공고 리뷰 요약 | MySQL 검색 도구 우선, 추후 embedding 보조 |

초기 MVP에서는 `service_guide`, `jeju_travel`을 프로젝트 내부 Markdown 또는 DB seed 문서로 시작한다.
OpenAI vector store/file search를 사용할 수도 있고, 자체 embedding 테이블을 둘 수도 있다.
게스트하우스/스텝 공고처럼 자주 바뀌는 운영 데이터는 동기화 비용이 있으므로 처음부터 외부 vector store에만 의존하지 않는다.

### 권장 DB 테이블

AI 대화 저장:

| 테이블 | 주요 컬럼 | 설명 |
|--------|----------|------|
| `ai_conversation` | `id`, `user_id`, `title`, `created_at`, `updated_at` | 로그인 사용자별 AI 대화방. 비로그인 1회성 대화는 저장하지 않아도 됨 |
| `ai_message` | `id`, `conversation_id`, `role`, `content`, `created_at` | 사용자/AI 메시지 저장 |
| `ai_message_card` | `id`, `message_id`, `type`, `target_id`, `title`, `description`, `image_url` | FE 카드 렌더링용 추천/출처 카드 |
| `ai_retrieval_log` | `id`, `message_id`, `source_type`, `source_id`, `score`, `snippet` | 어떤 근거를 사용했는지 디버깅/품질 개선용 |

문서 RAG를 DB로 관리할 경우:

| 테이블 | 주요 컬럼 | 설명 |
|--------|----------|------|
| `ai_knowledge_document` | `id`, `source_type`, `title`, `content`, `metadata`, `updated_at` | 서비스 안내/제주 관광 원문 |
| `ai_knowledge_chunk` | `id`, `document_id`, `chunk_index`, `content`, `embedding_ref`, `metadata` | 검색 단위 chunk. embedding은 외부 vector store ID 또는 자체 vector 컬럼 참조 |

### AI/리포트 단계별 DB 도입 계획

챗봇만 1회성으로 운영한다면 새 테이블 없이도 가능하지만, 대화 저장, 질문 트렌드, 사장님 리포트, 플랫폼 인사이트까지 만들 계획이므로 아래 테이블은 단계적으로 추가한다.
게스트하우스/스텝 공고 추천 자체는 기존 MySQL 운영 테이블을 조회하고, AI 관련 테이블은 "대화 이력", "분석 로그", "리포트 스냅샷"을 저장하는 용도로만 둔다.

#### 1단계: 통합 AI 챗봇 MVP

목표:
- FE AI 탭에서 질문을 보내면 실제 DB의 게스트하우스/스텝 공고 후보를 기반으로 답변한다.
- 답변 텍스트와 카드 데이터를 함께 반환한다.
- 로그인 사용자는 대화 기록을 다시 볼 수 있다.

필수 테이블:

| 테이블 | 필수 여부 | 이유 |
|--------|-----------|------|
| `ai_conversation` | 필수 | 로그인 사용자별 AI 대화방 목록 저장 |
| `ai_message` | 필수 | 사용자 질문과 AI 답변 저장 |
| `ai_message_card` | 권장 | AI 답변에 붙은 게스트하우스/스텝 공고/관광지 카드 재표시 |
| `ai_retrieval_log` | 권장 | 답변이 어떤 DB 후보/문서를 근거로 했는지 추적 |

1단계 API:

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/ai/chat` | 질문 전송, 답변/카드 반환, 로그인 시 대화 저장 |
| GET | `/api/v1/ai/conversations` | 내 AI 대화 목록 |
| GET | `/api/v1/ai/conversations/{conversationId}/messages` | 대화 메시지 조회 |
| DELETE | `/api/v1/ai/conversations/{conversationId}` | 대화 삭제 |

1단계 구현 순서:
1. `ai` 패키지, DTO, enum, 엔티티, repository 생성
2. `GuestHouseAiSearchService`, `StaffRecruitmentAiSearchService`를 기존 DB 조회 기반으로 구현
3. `AiIntentRouter`에서 질문 의도와 검색 조건 추출
4. `LlmAnswerService`에서 DB 후보를 받아 최종 답변 JSON 생성
5. FE AI 탭을 실제 채팅 UI로 전환

#### 2단계: 질문 로그와 트렌드 기반 리포트 준비

목표:
- 사용자가 AI 챗봇에서 무엇을 찾는지 누적한다.
- 사장님 리포트와 플랫폼 공통 인사이트의 원천 데이터를 만든다.

필수 테이블:

| 테이블 | 필수 여부 | 이유 |
|--------|-----------|------|
| `ai_user_question_log` | 필수 | 질문 의도, 지역, 키워드, 대상 도메인을 집계하기 위한 원천 로그 |
| `post_view_log` | 필수 | 게스트하우스/스텝 공고 상세 조회 수를 일관되게 집계 |

`ai_user_question_log` 저장 예시:

```json
{
  "userId": 7,
  "intent": "GUEST_HOUSE_RECOMMENDATION",
  "normalizedQuestion": "제주시 조용한 혼자 여행 게스트하우스 추천",
  "extractedRegion": "제주시",
  "extractedKeywords": ["조용한", "혼자 여행", "청결"],
  "targetType": "GUEST_HOUSE_POST"
}
```

2단계 구현 순서:
1. `POST /api/v1/ai/chat` 처리 후 질문 의도/키워드를 `ai_user_question_log`에 저장
2. 게스트하우스/스텝 공고 상세 조회 시 `post_view_log` 저장
3. 사용자 개인정보가 들어갈 수 있는 원문 질문은 리포트용으로 직접 노출하지 않고 정규화/키워드화한 값만 사용

#### 3단계: 리뷰 AI 요약과 사장님 운영 리포트

목표:
- 유저 상세 화면에는 리뷰 요약/대표 키워드를 가볍게 보여준다.
- 사장님 관리 페이지에는 조회/찜/문의/리뷰/AI 질문 기반 운영 개선 리포트를 보여준다.

필수 테이블:

| 테이블 | 필수 여부 | 이유 |
|--------|-----------|------|
| `review_keyword_summary` | 필수 | 리뷰 키워드와 유저용/사장님용 요약 저장 |
| `owner_report_snapshot` | 필수 | 사장님별 주간/월간 리포트 결과 저장 |

3단계 API:

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/guest-houses/{guestHousePostId}/review-keywords` | 유저용 리뷰 요약/키워드 |
| GET | `/api/v1/owner/reports/guest-houses/{guestHousePostId}` | 게스트하우스 운영 리포트 |
| GET | `/api/v1/owner/reports/staff-recruitments/{staffRecruitmentId}` | 스텝 공고 운영 리포트 |

3단계 구현 순서:
1. `review_keyword_summary`를 규칙 기반 키워드 분석으로 먼저 생성
2. 리뷰가 3개 이상인 게스트하우스 상세의 리뷰 목록 위에 유저용 요약 노출
3. `owner_report_snapshot`에 조회/찜/채팅 문의/지원/리뷰/AI 질문 지표를 주간 단위로 저장
4. 사장님 관리 페이지에서 리포트 카드와 개선 제안 표시
5. 이후 LLM을 붙여 리뷰 요약과 개선 제안 품질을 고도화

#### 4단계: 플랫폼 공통 인사이트 리포트

목표:
- 게하르방 전체에서 요즘 어떤 숙소/공고/여행 조건이 많이 언급되는지 사장님들에게 제공한다.
- 개별 사장님 리포트에 플랫폼 트렌드와의 비교 제안을 연결한다.

필수 테이블:

| 테이블 | 필수 여부 | 이유 |
|--------|-----------|------|
| `platform_insight_report` | 필수 | 주간/월간 플랫폼 인사이트 본문 저장 |
| `platform_insight_topic` | 필수 | 보고서 안의 핵심 트렌드 토픽 저장 |

4단계 구현 순서:
1. `ai_user_question_log`, `post_view_log`, `wish`, `review`, `application_record`를 주간 단위로 집계
2. 게스트하우스/스텝 공고 도메인별 트렌드 토픽 생성
3. `platform_insight_report`, `platform_insight_topic`에 스냅샷 저장
4. 사장님 관리 페이지의 리포트 영역에서 "이번 주 게하르방 트렌드"로 노출

#### 전체 구현 체크리스트

아래 순서대로 구현하면 챗봇, 리뷰 요약, 사장님 리포트, 플랫폼 인사이트가 같은 데이터 흐름 위에서 자연스럽게 이어진다.

##### A. 공통 enum/값 정의

| Enum | 값 | 사용 위치 |
|------|----|-----------|
| `AiIntent` | `SERVICE_GUIDE`, `JEJU_TRAVEL`, `GUEST_HOUSE_RECOMMENDATION`, `STAFF_RECRUITMENT_RECOMMENDATION`, `MIXED_TRAVEL_AND_GUEST_HOUSE`, `MIXED_GUEST_HOUSE_AND_STAFF`, `GENERAL_CHAT` | 질문 의도 분류, 질문 로그 |
| `AiMessageRole` | `USER`, `ASSISTANT`, `SYSTEM` | AI 메시지 저장 |
| `AiCardType` | `GUEST_HOUSE`, `STAFF_RECRUITMENT`, `TRAVEL_SPOT`, `SERVICE_GUIDE` | FE 카드 렌더링 |
| `AiSourceType` | `GUEST_HOUSE_POST`, `STAFF_RECRUITMENT`, `REVIEW`, `SERVICE_GUIDE`, `JEJU_TRAVEL` | retrieval log |
| `AiTargetType` | `GUEST_HOUSE_POST`, `STAFF_RECRUITMENT`, `PLATFORM` | 질문 로그, 조회 로그, 리포트 |
| `ReportPeriodType` | `DAILY`, `WEEKLY`, `MONTHLY` | 리포트 스냅샷 |
| `ReportDomain` | `GUEST_HOUSE`, `STAFF_RECRUITMENT`, `TRAVEL`, `ALL` | 플랫폼 인사이트 |

##### B. 1단계 DB 스키마 상세

`ai_conversation`

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | bigint PK | 대화 ID |
| `user_id` | bigint nullable | 로그인 사용자. 비로그인 대화 저장을 하지 않으면 nullable 불필요 |
| `title` | varchar(100) | 첫 질문 기반 제목 |
| `deleted` | boolean | 사용자 대화 삭제 처리 |
| `created_at` | datetime | 생성 시각 |
| `updated_at` | datetime | 마지막 메시지 시각 |

`ai_message`

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | bigint PK | 메시지 ID |
| `conversation_id` | bigint FK | 대화 ID |
| `role` | varchar(30) | `USER` / `ASSISTANT` |
| `content` | text | 메시지 본문 |
| `intent` | varchar(80) nullable | 사용자 메시지일 때 분류된 intent |
| `created_at` | datetime | 생성 시각 |

`ai_message_card`

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | bigint PK | 카드 ID |
| `message_id` | bigint FK | AI 답변 메시지 ID |
| `type` | varchar(50) | 카드 타입 |
| `target_id` | bigint nullable | DB 대상 ID. 관광지/가이드 카드처럼 내부 ID가 없으면 nullable |
| `title` | varchar(150) | 카드 제목 |
| `description` | varchar(500) | 카드 설명 |
| `image_url` | varchar(1000) nullable | 대표 이미지 |
| `sort_order` | int | 카드 표시 순서 |

`ai_retrieval_log`

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | bigint PK | 로그 ID |
| `message_id` | bigint FK | AI 답변 메시지 ID |
| `source_type` | varchar(50) | 근거 타입 |
| `source_id` | bigint nullable | DB 근거 ID |
| `score` | double nullable | 검색 점수 또는 정렬 점수 |
| `snippet` | varchar(1000) | LLM에 전달한 요약 근거 |

##### C. 1단계 백엔드 구현 상세

패키지:

```text
ai/
├── controller/AiChatController.java
├── service/AiConversationService.java
├── service/AiIntentRouter.java
├── service/GuestHouseAiSearchService.java
├── service/StaffRecruitmentAiSearchService.java
├── service/LlmAnswerService.java
├── domain/model/*
├── domain/vo/*
├── dto/request/AiChatRequest.java
├── dto/response/AiChatResponse.java
├── dto/response/AiChatCardResponse.java
└── repository/*
```

`AiChatRequest`:

```json
{
  "conversationId": 1,
  "message": "제주시에서 조용하고 혼자 쉬기 좋은 게하 추천해줘"
}
```

`AiChatResponse`:

```json
{
  "conversationId": 1,
  "messageId": 12,
  "answer": "제주시에서 조용한 분위기를 찾는다면 아래 게스트하우스를 먼저 볼 만해요.",
  "cards": [
    {
      "type": "GUEST_HOUSE",
      "targetId": 3,
      "title": "제주 달빛 게하",
      "description": "조용한 분위기, 리뷰 4.8점, 혼자 쉬기 좋다는 언급이 많아요.",
      "imageUrl": "/images/..."
    }
  ],
  "suggestedQuestions": [
    "가격 낮은 순으로 더 보여줘",
    "파티 없는 곳만 추천해줘"
  ]
}
```

서비스 책임:

| 서비스 | 책임 |
|--------|------|
| `AiConversationService` | 요청 orchestration, 메시지 저장, 검색/LLM 호출, 응답 저장 |
| `AiIntentRouter` | 질문 의도, 지역, 가격, 기간, 키워드 추출 |
| `GuestHouseAiSearchService` | 기존 게스트하우스 DB에서 후보 3~5개 압축 DTO 생성 |
| `StaffRecruitmentAiSearchService` | 기존 스텝 공고 DB에서 후보 3~5개 압축 DTO 생성 |
| `LlmAnswerService` | 검색 후보와 문서 근거를 넣어 최종 답변 JSON 생성 |

1단계에서는 LLM 실패 시에도 서비스가 완전히 죽지 않도록 fallback 답변을 둔다.
예를 들어 DB 후보는 찾았지만 LLM 호출이 실패하면, 백엔드가 템플릿 문장과 카드만 반환한다.

##### D. 2단계 DB/로그 구현 상세

`ai_user_question_log`

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | bigint PK | 질문 로그 ID |
| `user_id` | bigint nullable | 로그인 사용자 |
| `conversation_id` | bigint nullable | 연결된 AI 대화 |
| `message_id` | bigint nullable | 사용자 메시지 |
| `intent` | varchar(80) | 질문 의도 |
| `normalized_question` | varchar(500) | 개인정보를 줄인 정규화 질문 |
| `extracted_region` | varchar(100) nullable | 추출 지역 |
| `extracted_keywords_json` | json/text | 키워드 배열 |
| `target_type` | varchar(50) nullable | 질문 대상 |
| `created_at` | datetime | 생성 시각 |

`post_view_log`

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | bigint PK | 조회 로그 ID |
| `target_type` | varchar(50) | `GUEST_HOUSE_POST` / `STAFF_RECRUITMENT` |
| `target_id` | bigint | 게시글/공고 ID |
| `user_id` | bigint nullable | 로그인 사용자 |
| `session_id` | varchar(100) nullable | 비로그인 또는 중복 조회 방지용 |
| `created_at` | datetime | 조회 시각 |

조회 수 집계는 같은 사용자가 짧은 시간 안에 새로고침하는 것을 과도하게 세지 않도록, 서비스 레벨에서 같은 `targetType + targetId + userId/sessionId`의 30분 이내 중복 로그는 선택적으로 제외한다.

##### E. 3단계 리뷰 요약/운영 리포트 구현 상세

`review_keyword_summary` 생성 기준:

- `targetType = GUEST_HOUSE_POST`부터 구현한다.
- `Review.status = ACTIVE`만 사용한다.
- 리뷰가 3개 미만이면 `visible = false` 응답을 반환한다.
- 초기 버전은 규칙 기반 키워드 추출로 시작한다.
- 이후 LLM 요약을 붙일 때도 결과는 같은 테이블에 저장한다.

규칙 기반 키워드 예시:

| 내부 키워드 | 매칭 표현 | 유저용 태그 |
|-------------|-----------|-------------|
| `CLEAN` | 청결, 깨끗, 깔끔, 침구 | 청결해요 |
| `QUIET` | 조용, 소음 없음, 쉬기 | 조용한 편이에요 |
| `SOLO` | 혼자, 혼여, 1인 | 혼자 쉬기 좋아요 |
| `LOCATION` | 위치, 버스, 공항, 정류장 | 위치가 편해요 |
| `HOST` | 친절, 설명, 안내 | 안내가 친절해요 |
| `PARTY` | 파티, 교류, 사람들 | 교류하기 좋아요 |

유저용 API 응답:

```json
{
  "visible": true,
  "reviewCount": 8,
  "summary": "청결하고 조용하다는 후기가 많고, 혼자 여행한 유저도 편하게 머물렀다는 언급이 있어요.",
  "tags": ["청결해요", "조용한 편이에요", "혼자 쉬기 좋아요"]
}
```

`owner_report_snapshot`

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | bigint PK | 리포트 ID |
| `owner_id` | bigint | 사장님 ID |
| `target_type` | varchar(50) | 게스트하우스/스텝 공고 |
| `target_id` | bigint | 대상 ID |
| `period_type` | varchar(20) | `WEEKLY` / `MONTHLY` |
| `period_start` | date | 기간 시작 |
| `period_end` | date | 기간 종료 |
| `metrics_json` | json/text | 조회, 찜, 채팅, 지원, 리뷰 지표 |
| `insight_json` | json/text | AI 또는 규칙 기반 인사이트 |
| `created_at` | datetime | 생성 시각 |

운영 리포트 권한:

- 게스트하우스 리포트는 해당 `GuestHousePost.userId`와 요청 `userId`가 같아야 조회 가능하다.
- 스텝 공고 리포트는 해당 `StaffRecruitment.userId`와 요청 `userId`가 같아야 조회 가능하다.
- 관리자는 추후 별도 관리자 리포트 API를 만들기 전까지 사장님용 API로 우회하지 않는다.

##### F. 4단계 플랫폼 인사이트 구현 상세

`platform_insight_report`

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | bigint PK | 보고서 ID |
| `period_type` | varchar(20) | `WEEKLY` / `MONTHLY` |
| `period_start` | date | 기간 시작 |
| `period_end` | date | 기간 종료 |
| `target_domain` | varchar(50) | `GUEST_HOUSE`, `STAFF_RECRUITMENT`, `ALL` |
| `title` | varchar(150) | 보고서 제목 |
| `summary` | varchar(1000) | 전체 요약 |
| `content_json` | json/text | 상세 본문 |
| `created_at` | datetime | 생성 시각 |

`platform_insight_topic`

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | bigint PK | 토픽 ID |
| `report_id` | bigint FK | 보고서 ID |
| `rank` | int | 순위 |
| `topic` | varchar(150) | 토픽명 |
| `score` | double | 집계 점수 |
| `example_questions_json` | json/text | 익명화된 대표 질문 |
| `recommendation` | varchar(1000) | 사장님이 참고할 제안 |

플랫폼 인사이트는 최소 집계 기준을 둔다.
예를 들어 특정 기간의 질문 수나 리뷰 수가 너무 적으면 개별 사용자의 취향이 노출될 수 있으므로 보고서를 생성하지 않거나, 더 넓은 기간으로 합친다.

##### G. 배치/스케줄 기준

| 배치 | 주기 | 역할 |
|------|------|------|
| `ReviewKeywordSummaryJob` | 매일 새벽 또는 리뷰 변경 후 지연 실행 | 리뷰 키워드/요약 갱신 |
| `OwnerReportSnapshotJob` | 매주 월요일 새벽 | 사장님별 주간 리포트 생성 |
| `PlatformInsightReportJob` | 매주 월요일 새벽 | 플랫폼 공통 인사이트 생성 |

초기에는 Spring Scheduler로 시작하고, 서버가 여러 대로 늘어나면 배치 중복 실행 방지를 위해 별도 lock 테이블 또는 외부 스케줄러를 도입한다.

##### H. 테스트 기준

백엔드 테스트:

- `AiIntentRouterTest`: 질문별 intent/지역/키워드 추출
- `GuestHouseAiSearchServiceTest`: 지역/분위기/가격 조건에 맞는 후보 반환
- `StaffRecruitmentAiSearchServiceTest`: 근무 기간/혜택/직무 조건 후보 반환
- `AiConversationServiceTest`: 대화/메시지/카드 저장과 권한 검증
- `ReviewKeywordSummaryServiceTest`: 리뷰 3개 미만 숨김, 키워드 추출, 유저용 태그 변환
- `OwnerReportServiceTest`: 소유자만 리포트 조회 가능
- `PlatformInsightReportServiceTest`: 최소 집계 기준 미달 시 보고서 미생성

프론트 연동 테스트:

- AI 탭에서 질문 전송 후 답변/카드 렌더링
- 게스트하우스 상세 리뷰 섹션 위에 `visible = true`일 때만 요약 렌더링
- 사장님 리포트 화면에서 조회/찜/문의/리뷰 지표 카드 렌더링
- 플랫폼 인사이트 화면에서 토픽 리스트 렌더링

##### I. 구현 우선순위

가장 먼저 만들 실제 PR 단위:

1. BE: `ai_conversation`, `ai_message`, `ai_message_card`, `ai_retrieval_log` 엔티티/레포지토리 추가
2. BE: `/api/v1/ai/chat` skeleton과 템플릿 fallback 응답 구현
3. BE: 게스트하우스/스텝 공고 DB 검색 서비스 구현
4. BE: LLM API 연동과 JSON 응답 파싱
5. FE: AI 탭 채팅 UI와 카드 렌더링
6. BE: `ai_user_question_log`, `post_view_log` 추가
7. BE/FE: 리뷰 AI 요약 유저용 API와 리뷰 섹션 위 노출
8. BE/FE: 사장님 운영 리포트 화면
9. BE/FE: 플랫폼 공통 인사이트 화면

### 리뷰 AI 키워드 분석 설계

리뷰 본문은 별점보다 더 많은 신호를 담고 있으므로, AI로 키워드와 요약을 뽑아 사용자 화면과 사장님 화면에 다르게 제공한다.
같은 분석 결과를 재사용하되, 유저에게는 선택을 돕는 가벼운 태그를 보여주고 사장님에게는 운영 개선용 상세 리포트를 보여준다.

분석 대상:

| 대상 | 현재/향후 | 설명 |
|------|-----------|------|
| 게스트하우스 리뷰 | 현재 | 숙소 경험, 청결, 위치, 소음, 분위기, 체크인 등 |
| 스텝 공고 리뷰 | 향후 | 근무 강도, 숙소 제공, 교육, 휴무, 소통, 업무 범위 등 |

권장 DB 테이블:

| 테이블 | 주요 컬럼 | 설명 |
|--------|----------|------|
| `review_keyword_summary` | `id`, `target_type`, `target_id`, `review_count`, `positive_keywords_json`, `negative_keywords_json`, `neutral_keywords_json`, `user_summary`, `owner_summary`, `owner_recommendation`, `period_start`, `period_end`, `created_at`, `updated_at` | 리뷰 AI 분석 결과를 대상별로 저장 |

표시 정책:

| 제공 대상 | 표시 내용 | 목적 |
|-----------|-----------|------|
| 일반 유저 | 대표 키워드 태그, 한 줄 요약, 긍정/중립 중심 표현 | 숙소/공고 선택을 빠르게 돕기 |
| 사장님 | 긍정 키워드, 개선 키워드, 반복 이슈, AI 개선 제안 | 상세 페이지/운영 방식 개선 |
| 플랫폼 리포트 | 전체 트렌드 키워드와 수요 변화 | 시장 흐름 파악 |

유저용 MVP 노출 규칙:

- 게스트하우스 상세 화면의 리뷰 목록 바로 위에만 노출한다.
- 목록 카드나 상세 상단에는 처음부터 노출하지 않고, 리뷰 섹션의 보조 정보로 시작한다.
- `reviewCount >= 3`인 경우에만 리뷰 요약과 대표 키워드를 보여준다.
- `reviewCount < 3`이면 요약 영역을 숨기거나 "아직 리뷰 요약을 만들기에는 후기가 부족해요"로 처리한다.
- 부정 키워드는 유저 화면에 직접 태그로 노출하지 않고, 필요할 때만 "체크해볼 포인트"처럼 중립적으로 표현한다.

유저 화면 예시:

```json
{
  "targetType": "GUEST_HOUSE_POST",
  "targetId": 12,
  "tags": ["청결해요", "혼자 쉬기 좋아요", "위치가 편해요", "조용한 편이에요"],
  "summary": "리뷰에서는 청결, 조용한 분위기, 혼자 여행 편의성이 자주 언급돼요."
}
```

사장님 화면 예시:

```json
{
  "targetType": "GUEST_HOUSE_POST",
  "targetId": 12,
  "positiveKeywords": [
    { "keyword": "청결", "count": 14, "examples": ["침구가 깨끗했어요"] },
    { "keyword": "위치", "count": 9, "examples": ["버스 정류장이 가까워요"] }
  ],
  "negativeKeywords": [
    { "keyword": "소음", "count": 4, "examples": ["밤에 문 닫는 소리가 들렸어요"] },
    { "keyword": "체크인 안내", "count": 3, "examples": ["입실 안내가 조금 헷갈렸어요"] }
  ],
  "summary": "청결과 위치 평가는 좋지만, 소음과 체크인 안내 관련 언급이 반복됩니다.",
  "recommendation": "상세 페이지에 소등 시간, 조용한 시간대, 체크인 절차를 더 구체적으로 추가하세요."
}
```

권장 API:

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| GET | `/api/v1/guest-houses/{guestHousePostId}/review-keywords` | 선택 | 유저용 게스트하우스 리뷰 키워드 조회 |
| GET | `/api/v1/owner/reports/guest-houses/{guestHousePostId}/review-keywords` | 사장님 | 사장님용 게스트하우스 리뷰 키워드 분석 조회 |
| GET | `/api/v1/staff-recruitment/{staffRecruitmentId}/review-keywords` | 선택 | 향후 유저용 스텝 공고 리뷰 키워드 조회 |
| GET | `/api/v1/owner/reports/staff-recruitments/{staffRecruitmentId}/review-keywords` | 사장님 | 향후 사장님용 스텝 공고 리뷰 키워드 분석 조회 |

분석은 리뷰 작성 요청마다 실시간으로 LLM을 호출하지 않는다.
초기에는 리뷰가 일정 개수 이상 쌓였을 때 수동/배치로 생성하고, 이후에는 하루 1회 또는 주간 단위로 갱신한다.
리뷰 수가 적을 때는 개별 사용자가 식별되지 않도록 키워드 요약을 숨기거나 "아직 리뷰가 충분하지 않아요"로 처리한다.

### 사장님 운영 리포트 설계

AI 챗봇과 리뷰 데이터가 쌓이면, 각 사장님에게 "내 게시글이 얼마나 보고 있고, 사람들이 무엇을 궁금해하고, 어떤 개선 포인트가 있는지"를 보여주는 운영 리포트로 확장한다.
이 기능은 일반 사용자용 AI 챗봇과 별도 API로 두되, 분석 재료는 AI 질문 로그/리뷰/조회/찜/채팅/지원 데이터를 함께 사용한다.

운영 리포트 대상:

| 대상 | 설명 |
|------|------|
| 게스트하우스 게시글 | 숙소 조회, 찜, 리뷰, 사용자 질문, 채팅 문의 기반 인사이트 |
| 스텝 공고 | 공고 조회, 찜, 지원 전환, 채팅 문의, 향후 근무 경험 리뷰 기반 인사이트 |

현재 DB에서 바로 활용 가능한 데이터:

| 지표 | 테이블/엔티티 | 비고 |
|------|---------------|------|
| 찜 수 | `wish` / `Wish` | 게스트하우스/스텝 공고 관심도 |
| 채팅 문의 | `chat_room`, `chat_message` | 해당 게시글/공고와 연결된 사용자 질문 흐름 |
| 지원 수 | `application_record` | 스텝 공고 지원 전환 지표 |
| 리뷰 평점/본문 | `review`, `review_image` | 현재 게스트하우스 리뷰, 향후 스텝 공고 리뷰 |
| 스텝 공고 조회 수 | `staff_recruitment.view_count` | 현재 스텝 공고에만 존재 |

추가가 필요한 데이터:

| 테이블 | 주요 컬럼 | 설명 |
|--------|----------|------|
| `post_view_log` | `id`, `target_type`, `target_id`, `user_id`, `session_id`, `created_at` | 게스트하우스/스텝 공고 상세 조회 로그. 비로그인 사용자는 `session_id`만 저장 |
| `owner_report_snapshot` | `id`, `owner_id`, `target_type`, `target_id`, `period_start`, `period_end`, `metrics_json`, `insight_json`, `created_at` | 주간/월간 리포트 스냅샷. LLM 비용 절감을 위해 배치 생성 결과 저장 |

권장 API:

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| GET | `/api/v1/owner/reports/guest-houses/{guestHousePostId}` | 사장님 | 내 게스트하우스 운영 리포트 조회 |
| GET | `/api/v1/owner/reports/staff-recruitments/{staffRecruitmentId}` | 사장님 | 내 스텝 공고 운영 리포트 조회 |
| POST | `/api/v1/owner/reports/guest-houses/{guestHousePostId}/refresh` | 사장님 | 필요 시 최신 리포트 재생성 |
| POST | `/api/v1/owner/reports/staff-recruitments/{staffRecruitmentId}/refresh` | 사장님 | 필요 시 최신 리포트 재생성 |

리포트 응답은 숫자 지표와 AI 인사이트를 분리한다.

```json
{
  "targetType": "GUEST_HOUSE_POST",
  "targetId": 12,
  "periodStart": "2026-07-01",
  "periodEnd": "2026-07-07",
  "metrics": {
    "viewCount": 320,
    "wishCount": 41,
    "chatInquiryCount": 18,
    "reviewCount": 7,
    "averageRating": 4.7
  },
  "insights": [
    {
      "type": "QUESTION_TREND",
      "title": "혼자 여행 관련 질문이 많아요",
      "summary": "최근 문의에서 1인실, 조용한 분위기, 밤 소음 질문이 반복됩니다.",
      "recommendation": "상세 소개에 1인 여행자 안내와 소음 관리 방식을 추가하는 것이 좋아요."
    },
    {
      "type": "REVIEW_SIGNAL",
      "title": "청결 평가는 좋고, 체크인 안내는 보강이 필요해요",
      "summary": "리뷰에서는 청결 키워드가 긍정적으로 많이 등장하지만, 체크인 동선 문의가 반복됩니다.",
      "recommendation": "체크인 시간, 셀프 체크인 여부, 늦은 입실 안내를 상단에 노출하세요."
    }
  ]
}
```

LLM 분석에 넣는 원문은 최소화한다.
채팅 메시지와 AI 질문 로그는 개인정보를 마스킹하고, 해당 사장님의 게시글/공고에 연결된 내용만 사용한다.
리포트 생성 시 LLM에는 개별 사용자 식별자 대신 질문 유형, 키워드, 익명화된 대표 문장, 집계 지표를 전달한다.

운영 리포트에서 만들 인사이트:

| 인사이트 | 설명 |
|----------|------|
| 조회/찜 흐름 | 전주 대비 조회, 찜, 문의 증감 |
| 문의 질문 TOP N | 사용자들이 반복해서 묻는 주제 |
| 리뷰 긍정/부정 키워드 | 청결, 소음, 위치, 사장님 응대, 근무 강도 등 |
| 상세 페이지 보강 포인트 | 사용자가 자주 묻지만 게시글에 없는 정보 |
| 전환 개선 제안 | 조회 대비 찜/채팅/지원이 낮을 때 문구, 이미지, 조건 개선 제안 |

### 플랫폼 공통 인사이트 리포트 설계

개별 사장님 리포트와 별도로, 게하르방 전체에서 사람들이 요즘 어떤 게스트하우스/스텝 공고를 찾는지 묶어주는 공통 인사이트 리포트를 만든다.
예를 들어 "요즘 혼자 여행, 조용한 숙소, 여성 도미토리, 숙식 제공 단기 스텝 질문이 늘고 있다"처럼 사장님들이 시장 흐름을 이해하도록 돕는다.

이 리포트는 특정 사장님의 채팅 원문을 그대로 보여주지 않고, 플랫폼 단위로 익명화/집계한 트렌드만 제공한다.

주요 데이터 소스:

| 데이터 | 활용 방식 |
|--------|-----------|
| AI 챗봇 질문 | 사용자가 자연어로 찾는 지역, 분위기, 가격, 여행 스타일, 스텝 조건 추출 |
| 검색/필터 로그 | 지역, 날짜, 가격, 성별, 근무 기간, 혜택 조건 선호도 |
| 찜 데이터 | 실제 관심 전환이 일어난 게시글/공고 특성 |
| 리뷰 | 만족/불만 키워드, 재방문 의향, 근무 경험 평가 |
| 채팅 문의 | 상세 페이지에 부족한 정보와 실제 예약/지원 전 질문 |
| 지원 데이터 | 스텝 공고 조회 대비 지원 전환 |

권장 DB 테이블:

| 테이블 | 주요 컬럼 | 설명 |
|--------|----------|------|
| `ai_user_question_log` | `id`, `user_id`, `intent`, `normalized_question`, `extracted_region`, `extracted_keywords`, `target_type`, `created_at` | AI 챗봇 질문을 분석 가능한 형태로 저장. `user_id`는 nullable |
| `platform_insight_report` | `id`, `period_type`, `period_start`, `period_end`, `target_domain`, `title`, `summary`, `content_json`, `created_at` | 주간/월간 플랫폼 인사이트 보고서 |
| `platform_insight_topic` | `id`, `report_id`, `rank`, `topic`, `score`, `example_questions_json`, `recommendation` | 보고서 안의 핵심 트렌드 주제 |

권장 API:

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| GET | `/api/v1/owner/reports/platform-insights` | 사장님 | 플랫폼 공통 인사이트 리포트 조회 (`?domain=GUEST_HOUSE&periodType=WEEKLY`) |
| GET | `/api/v1/owner/reports/platform-insights/{reportId}` | 사장님 | 특정 인사이트 리포트 상세 조회 |

리포트 예시:

```json
{
  "periodType": "WEEKLY",
  "periodStart": "2026-07-01",
  "periodEnd": "2026-07-07",
  "targetDomain": "GUEST_HOUSE",
  "title": "이번 주 게스트하우스 관심 트렌드",
  "summary": "혼자 여행, 조용한 숙소, 여성 전용 객실, 늦은 체크인 관련 질문이 많았습니다.",
  "topics": [
    {
      "rank": 1,
      "topic": "혼자 쉬기 좋은 조용한 숙소",
      "score": 92,
      "exampleQuestions": [
        "혼자 가도 어색하지 않은 게하 추천해줘",
        "파티 없는 조용한 곳 있어?"
      ],
      "recommendation": "혼자 온 손님 비율, 소등 시간, 공용 공간 분위기를 상세 페이지에 적어두면 좋아요."
    },
    {
      "rank": 2,
      "topic": "뚜벅이 이동 편의",
      "score": 84,
      "exampleQuestions": [
        "버스로 갈 수 있는 숙소 알려줘",
        "공항에서 가까운 게하 있어?"
      ],
      "recommendation": "공항/버스정류장 이동 시간과 주변 편의시설을 이미지나 문장으로 보강하세요."
    }
  ]
}
```

개별 운영 리포트와 플랫폼 인사이트는 함께 연결한다.
예를 들어 플랫폼 리포트에서 "혼자 여행" 수요가 높게 나오고, 특정 게스트하우스 상세 페이지에 1인 여행자 안내가 부족하면 해당 사장님 리포트에서 "현재 트렌드 대비 보강할 항목"으로 제안한다.

구현 순서:

1. AI 챗봇 질문을 `ai_user_question_log`에 의도/지역/키워드 단위로 저장
2. 게스트하우스 상세 조회용 `post_view_log` 추가
3. 사장님용 단일 게시글 운영 리포트 API를 먼저 구현
4. 주간 배치로 `owner_report_snapshot` 생성
5. 플랫폼 공통 인사이트 리포트를 주간 배치로 생성
6. 개별 리포트에 플랫폼 트렌드와의 비교/추천 문구 연결

### LLM 프롬프트 원칙

시스템 프롬프트 핵심:

```text
너는 게하르방 AI 여행 도우미다.
게하르방 서비스 사용법, 제주 여행, 게스트하우스 추천, 스텝 공고 추천을 돕는다.
제공된 DB 후보가 있으면 그 후보 안에서만 게스트하우스/스텝 공고를 추천한다.
DB에 없는 게시글이나 공고를 만들어내지 않는다.
최신성이 중요한 관광지 영업시간, 입장료, 교통 정보는 확인이 필요하다고 말한다.
추천할 때는 이유를 짧고 구체적으로 설명하고, 카드에 연결할 수 있는 대상 ID를 유지한다.
```

### 처리 흐름

```
POST /api/v1/ai/chat
  → AiChatController
  → AiConversationService
    1. userId가 있으면 대화 생성/조회
    2. 사용자 메시지 저장
    3. AiIntentRouter로 의도 분류
    4. 의도에 따라 검색 도구 실행
       - 게스트하우스: GuestHouseAiSearchService
       - 스텝 공고: StaffRecruitmentAiSearchService
       - 서비스 안내: ServiceGuideRetriever
       - 제주 관광: JejuTravelRetriever
    5. 검색 후보/문서 조각을 LlmAnswerService에 전달
    6. 최종 답변, 카드, 추천 후속 질문 생성
    7. AI 메시지와 카드/검색 로그 저장
  → AiChatResponse 반환
```

혼합 질문 예시:

```
"비 오는 날 제주에서 갈 만한 곳이랑 근처 조용한 게하 추천해줘"
  → Intent: MIXED_TRAVEL_AND_GUEST_HOUSE
  → JejuTravelRetriever: 비 오는 날 관광지 후보
  → GuestHouseAiSearchService: 조용한 분위기 + 리뷰 좋은 게스트하우스 후보
  → LLM: 여행 코스 설명 + 게스트하우스 추천 이유 생성
```

스텝 공고 질문 예시:

```
"2주 정도 일할 수 있고 숙식 제공되는 스텝 공고 있어?"
  → Intent: STAFF_RECRUITMENT_RECOMMENDATION
  → StaffRecruitmentAiSearchService:
     - workingPeriod = 단기
     - employeeBenefits/content/job에 "숙식" 키워드 검색
     - ACTIVE 공고만 후보
  → LLM: 후보 중 조건이 잘 맞는 순서로 설명
```

### MVP 구현 순서

1. FE `app/(tabs)/ai.tsx`를 실제 채팅 UI로 전환
2. BE `ai` 패키지와 `POST /api/v1/ai/chat` 추가
3. AI 대화 저장 테이블 없이 1회성 응답 MVP 구현
4. `GuestHouseAiSearchService`, `StaffRecruitmentAiSearchService`를 MySQL QueryDSL 기반으로 구현
5. 서비스 안내/제주 관광은 짧은 Markdown 문서 RAG 또는 임시 static knowledge로 시작
6. LLM API 연동 및 답변 JSON schema 고정
7. 로그인 사용자 대화 저장, 카드 저장, retrieval log 추가
8. 필요 시 streaming 응답, vector store 동기화, 관광지 데이터 확장

---

## 13. 요청 처리 흐름

### 인증이 필요한 API

```
HTTP Request
  └── Authorization: Bearer {accessToken}
        │
        ▼
  nginx (포트 80/443)
        │
        ▼
  Spring Boot Dispatcher Servlet
        │
        ▼
  UserIdResolver (@UserId 파라미터 처리)
    → 토큰 파싱 → userId 추출
        │
        ▼
  Controller
    → Service 호출 (userId 전달)
        │
        ▼
  Service
    → Repository로 DB 조회/저장
    → 예외 발생 시 GuestHouseException throw
        │
        ▼
  Controller → ResponseEntity 반환
        │
        ▼
  (예외 발생 시) GlobalExceptionHandler → 에러 응답 반환
```

### 파일 업로드 흐름 (이미지/인증서)

```
POST /api/v1/application (multipart/form-data)
  → ApplicationController
  → ImageService.save(file)
    → IMAGE_DIRECTORY_PATH + fileName으로 저장
      (경로: /home/geharbang/images/ → Docker 볼륨 → ./nginx/images/)
  → 저장된 경로를 imageUrl로 Application 엔티티에 기록

GET /images/application/{fileName}
  → nginx가 /home/jeju/images/{fileName} 파일을 직접 서빙
  (Spring Boot를 거치지 않음)
```

---

## 14. 인프라 및 실행 환경

운영 환경은 **Mac mini self-hosted runner + Docker 컨테이너** 기반으로 구성되어 있다.
Spring Boot 애플리케이션, MySQL, nginx가 각각 별도 컨테이너로 실행되고, `guesthouse` Docker network를 통해 통신한다.

### 전체 구성

```
Client
  │
  ▼
nginx container: server-nginx
  - host 80/443 → container 80/443
  - HTTPS 종료, API reverse proxy, 정적 파일 서빙
  │
  ├── /api/*, /swagger-ui/*, /v3/api-docs
  │       ▼
  │   Spring Boot container: server-dev
  │     - host 8080 → container 8080
  │     - image: server:geharbang
  │     - profile: dev
  │
  └── /images/*, /files/certifications/*
          ▼
      nginx mounted directory

Spring Boot container
  │
  ▼
MySQL container: server-mysql
  - current running image: mysql:8.0
  - internal port: 3306
```

### Docker 구성

서버 운영 파일은 배포 서버의 `/Users/brains/guesthouse/jeju` 기준으로 관리된다.

| 구성 | 컨테이너 | 역할 |
|------|----------|------|
| `server-docker-compose.yml` | `server-dev` | Spring Boot JAR 실행 |
| `mysql-docker-compose.yml` | `server-mysql` | MySQL 데이터베이스 |
| `nginx-docker-compose.yml` | `server-nginx` | HTTPS, reverse proxy, 정적 파일 서빙 |
| `server-dockerfile` | `server:geharbang` image | `dev.jar`를 Java 21로 실행 |
| `deploy.sh` | - | 서버 이미지 빌드 후 `server-dev` 재생성 |

`server-dev`, `server-mysql`, `server-nginx`는 모두 외부 Docker network인 `guesthouse`에 연결된다.
GitHub Actions 배포 과정에서는 네트워크가 없으면 생성하고, `server-mysql` 컨테이너가 중지되어 있으면 다시 시작한다.

`deploy.sh`는 Docker Desktop이 설치된 Mac mini self-hosted runner에서 실행되므로, Docker credential helper 경로를 명시적으로 PATH에 추가한다.
이 설정이 없으면 `docker build --no-cache`가 base image metadata를 조회할 때 `docker-credential-desktop`을 찾지 못해 build가 실패할 수 있다.

```sh
export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"
```

### Spring Profile

| profile | 설정 파일 | 용도 | DB |
|---------|----------|------|----|
| `local` | `application-local.yml` | 로컬 개발 기본값 | MySQL |
| `dev` | `application-dev.yml` | 배포 서버 | MySQL |
| `test` | `src/test/resources/application-test.yml` | 테스트 | H2 in-memory |

`src/main/resources/application.yml`의 기본 profile은 `local`이다.
서버 컨테이너는 `server-dockerfile`에서 `-Dspring.profiles.active=dev`로 실행된다.

### DB 구성

운영/로컬 개발 DB는 MySQL을 사용한다.

| 항목 | 값 |
|------|----|
| MySQL image | 현재 실행 컨테이너는 `mysql:8.0` (`mysql-docker-compose.yml`은 `mysql:8.4.0` 선언) |
| 컨테이너 이름 | `server-mysql` |
| Docker network | `guesthouse` |
| 데이터 볼륨 | `./db/data:/var/lib/mysql` |
| 설정 볼륨 | `./db/config:/etc/mysql/conf.d` |
| 초기화 SQL 볼륨 | `./db/init:/docker-entrypoint-initdb.d` |
| Hibernate DDL | `ddl-auto: update` |
| Dialect | `org.hibernate.dialect.MySQL8Dialect` |
| Timezone | `Asia/Seoul` |

애플리케이션은 `.env`에서 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`를 읽어 DB에 연결한다.
테스트는 H2 in-memory DB를 사용하므로 MySQL 컨테이너 없이도 `./gradlew test`가 동작한다.

### 파일 저장 및 nginx 볼륨

업로드 파일은 Spring Boot 컨테이너 안에서 로컬 경로에 저장되지만, 실제로는 nginx 디렉토리와 Docker volume으로 공유된다.

| 파일 종류 | Spring Boot 저장 경로 | nginx 서빙 경로 | 외부 URL 예시 |
|----------|----------------------|----------------|--------------|
| 이미지 | `/home/geharbang/images` | `/home/jeju/images` | `/images/{fileName}` |
| 인증서 | `/home/geharbang/certs` | `/home/jeju/certs` | `/files/certifications/{fileName}` |

Spring Boot는 DB에 파일의 URL 경로만 저장하고, 파일 다운로드/조회 요청은 nginx가 직접 처리한다.

### CI/CD 흐름

```
push to dev or dev-test
  → GitHub Actions ubuntu runner에서 ./gradlew build
  → build/libs/dev.jar artifact 업로드
  → self-hosted runner(Mac mini)에서 artifact 다운로드
  → /Users/brains/guesthouse/jeju/archive/build/libs/dev.jar 로 복사
  → artifact 파일 존재 여부 확인
  → Docker Desktop 및 server-mysql 상태 확인
  → /Users/brains/guesthouse/jeju/deploy.sh 실행
  → server:geharbang 이미지 no-cache 재빌드
  → server-dev 컨테이너 재생성
```

배포 성공/실패는 Discord webhook으로 알림을 보낸다.
`dev` 브랜치 push 이후 새 코드가 운영에 반영되려면 artifact 복사, Docker image 재빌드, `server-dev` 컨테이너 재생성이 모두 성공해야 한다.

### 배포 반영 확인

배포 후에는 아래 순서로 실제 반영 여부를 확인한다.

```bash
# 서버 상태
curl -s http://localhost:8080/actuator/health

# 공개 목록 API가 실제 운영 데이터로 200을 반환하는지 확인
curl -i "http://localhost:8080/api/v1/guest-houses?pageNumber=0"
curl -i "http://localhost:8080/api/v1/staff-recruitment?pageNumber=0"

# 공개 상세 API가 200을 반환하고 isWished 필드를 포함하는지 확인
curl -i "http://localhost:8080/api/v1/guest-houses/1/details"
curl -i "http://localhost:8080/api/v1/staff-recruitment/1/details"

# 찜 많은순 정렬이 200을 반환하는지 확인
curl -i -G "http://localhost:8080/api/v1/guest-houses" --data-urlencode "pageNumber=0" --data-urlencode "sort=찜_많은순"
curl -i -G "http://localhost:8080/api/v1/staff-recruitment" --data-urlencode "pageNumber=0" --data-urlencode "sort=찜_많은순"

# Swagger 문서 반영 여부
curl -s http://localhost:8080/v3/api-docs

# 찜 API 반영 여부
curl -i -X POST http://localhost:8080/api/v1/wish/guest-houses/1
curl -i "http://localhost:8080/api/v1/wish/guest-houses/my?pageNumber=0"
curl -i "http://localhost:8080/api/v1/wish/staff-recruitment/my?pageNumber=0"
# 비로그인 요청이면 401 LOGIN_REQUIRED가 정상이다. 404면 아직 엔드포인트가 반영되지 않은 것이다.

# 새 이미지/JAR 반영 여부
docker image inspect server:geharbang --format 'Created={{.Created}} Id={{.Id}}'
docker exec server-dev stat /server/dev.jar
```

`server-dev`가 재시작되었더라도 Docker image가 새로 빌드되지 않았다면 이전 JAR로 뜰 수 있다.
이 경우 `docker image inspect server:geharbang`의 생성 시각과 `docker exec server-dev stat /server/dev.jar` 결과를 함께 확인한다.
Swagger와 실제 API 응답 모두에서 게스트하우스 목록/상세 DTO의 찜 필드는 `isWished`로 내려가야 한다.

배포 구조에서 `server-mysql`, `server-nginx`는 애플리케이션 코드 변경만으로 재시작하지 않는다.
API 코드 변경은 새 JAR를 포함한 `server:geharbang` image를 다시 만들고 `server-dev`만 재생성하는 방식으로 반영한다.

---

## 15. API 문서 (Swagger)

`springdoc-openapi`를 사용하며, nginx를 통해 외부에서 접근 가능.

**접속 URL:** `https://geharbang.org/swagger-ui/index.html`

**인증 방법:**
1. 우측 상단 **Authorize 🔒** 클릭
2. 로그인 후 발급받은 `accessToken` 입력 (`Bearer` 없이 토큰값만)
3. 이후 모든 API 호출에 `Authorization: Bearer {token}` 헤더 자동 첨부

**설정 구조:**

| 파일 | 역할 |
|------|------|
| `common/config/OpenApiConfig.java` | JWT Bearer Authorize 버튼 설정, 명세서 기본 정보 |
| `common/config/UserIdParameterCustomizer.java` | `@UserId` 파라미터를 Swagger UI에서 전역 숨김 처리 |
| `wish/controller/WishController.java` | `Wish` 태그와 찜 추가/삭제 API 설명 |
| `wish/dto/response/WishResponse.java` | 찜 추가 응답의 `wishId` schema 설명 |

`@UserId`는 JWT 토큰에서 자동 주입되는 파라미터로, `ParameterCustomizer`를 구현해 컨트롤러 31곳을 수정하지 않고 전역으로 숨김 처리.
