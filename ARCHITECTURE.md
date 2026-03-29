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
11. [요청 처리 흐름](#11-요청-처리-흐름)

---

## 1. 서비스 기능 전체 개요

Geharbang은 **제주 게스트하우스 스텝 구인/구직 플랫폼**이다.
게스트하우스 운영자는 숙소를 등록하고 스텝을 모집하고, 구직자는 지원서를 작성해서 공고에 지원한다.

### 사용자 유형

| 유형 | 조건 | 가능한 기능 |
|------|------|-------------|
| 비로그인 | - | 게스트하우스/공고 목록 조회, 추천 조회 |
| 로그인 (사용자) | 소셜 로그인 | 지원서 작성, 공고 지원, 찜하기, 내 정보 조회 |
| 운영자 | 인증서 심사 승인 후 Role = 운영자 | 게스트하우스 등록, 공고 등록, 지원자 관리 |

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
- 지원서 존재 여부 확인 API 별도 제공 (`/my/exist`)

#### 공고 지원 (Application Record)
- 특정 스텝 구인 공고에 지원서를 제출하는 행위
- 공고마다 추가 질문이 있을 수 있고, 지원 시 답변 포함
- 운영자는 지원자 목록 조회 + 합격/불합격 처리 가능
- 지원자는 내 지원 내역 조회 가능 (합격 필터링, 페이지네이션)

#### 운영자 인증 (Certificate)
- 영업신고증 또는 관광사업등록증 파일 업로드 후 제출
- 관리자가 심사 → 승인/거부
- 승인 시 유저 Role이 운영자로 변경되지는 않음 (Certificate Status로 관리)
- 인증서 상태: 검토 대기 → 승인 완료 / 거부됨

#### 게스트하우스 게시글 (GuestHousePost)
- 운영자가 숙소 게시글 등록/수정/삭제
- 다양한 필터로 목록 조회: 지역, 가격, 파티 유형, 방 유형, 인원, 편의시설, 분위기
- 정렬: 최신순 / 조회순 / 찜 많은순
- 지역 추천 (랜덤)
- 게시글 상태: ACTIVE / INACTIVE

#### 스텝 구인 공고 (StaffRecruitment)
- 운영자가 공고 등록/수정/삭제
- 다양한 필터로 목록 조회: 지역, 근무 형태, 근무 요일, 휴무일, 기간, 스케줄, 성별
- 공고별 추가 질문 설정 가능
- 지역 추천 (랜덤)
- 찜하기 가능

#### 찜 (Wish)
- 스텝 구인 공고 찜 추가 / 삭제

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
├── certificate/             # 운영자 인증서
├── guestHousePost/          # 게스트하우스 게시글
├── staffrecruitment/        # 스텝 구인 공고
└── wish/                    # 찜 목록
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
| `certificate` | 운영자 인증서 제출 및 관리자 심사 |
| `guestHousePost` | 게스트하우스 숙소 게시글 CRUD |
| `staffrecruitment` | 게스트하우스 스텝 구인 공고 CRUD |
| `wish` | 공고 찜하기 기능 |

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

### 4-1. 예외 처리

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

### 4-2. @UserId 어노테이션

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

### 4-3. 설정 파일

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
  └── 1:N → certificate (Certificate)       # 운영자 인증서
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
| GET | `/api/v1/oauth/kakao/callback` | 불필요 | 카카오 인가코드 처리 → accessToken 발급 |
| GET | `/api/v1/oauth/google/login` | 불필요 | 구글 로그인 URI 반환 |
| GET | `/api/v1/oauth/google/callback` | 불필요 | 구글 인가코드 처리 → accessToken 발급 |

### 유저 (User)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| GET | `/api/v1/user/profile` | 필요 | 내 프로필 조회 (이름, 이미지, 운영자 여부, 인증 심사 상태) |

### 지원서 (Application)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/api/v1/application/` | 필요 | 지원서 작성/저장 |
| GET | `/api/v1/application/my` | 필요 | 내 지원서 조회 |
| GET | `/api/v1/application/my/exist` | 필요 | 내 지원서 존재 여부 확인 |
| POST | `/api/v1/application/images` | 필요 | 지원서 프로필 이미지 업로드 (1장) |
| POST | `/api/v1/application/staff-recruitment/{recruitmentId}` | 필요 | 특정 공고에 지원서 제출 |

### 지원 내역 (Application Record)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| GET | `/api/v1/application-records/my` | 필요 | 내 지원 내역 조회 (합격 필터, 페이지네이션) |
| GET | `/api/v1/application-records/{recordId}` | 필요 | 특정 지원 내역 상세 조회 |
| GET | `/api/v1/application-records/questions/{recordId}` | 필요 | 지원 내역의 질문/답변 조회 |
| GET | `/api/v1/application-records/{id}/all` | 필요 | (운영자) 특정 공고의 전체 지원자 목록 |
| POST | `/api/v1/application-records/{recordId}` | 필요 | (운영자) 합격/불합격 처리 |

### 운영자 인증 (Certificate)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/api/v1/certificate/file-upload` | 필요 | 인증서 파일 업로드 |
| POST | `/api/v1/certificate/owner` | 필요 | 인증서 제출 (심사 요청) |
| GET | `/api/v1/certificate` | 필요 | 제출된 인증서 목록 조회 |
| GET | `/api/v1/certificate/{certificateId}` | 필요 | 인증서 상세 조회 |
| POST | `/api/v1/certificate/{certificateId}` | 필요 | (관리자) 인증서 승인/거부 |

### 게스트하우스 게시글 (GuestHousePost)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| GET | `/api/v1/guest-houses/` | 선택 | 게시글 목록 (필터/정렬) |
| GET | `/api/v1/guest-houses/recommendation` | 불필요 | 지역별 랜덤 추천 |
| GET | `/api/v1/guest-houses/{id}/details` | 불필요 | 게시글 상세 조회 |
| POST | `/api/v1/guest-houses/` | 필요 | 게스트하우스 게시글 등록 |
| GET | `/api/v1/guest-houses/owner` | 필요 | 내 게스트하우스 게시글 목록 |
| PATCH | `/api/v1/guest-houses/{id}` | 필요 | 게시글 상태 변경 (ACTIVE/INACTIVE) |
| DELETE | `/api/v1/guest-houses/{id}` | 필요 | 게시글 삭제 |

**조회 필터:** `sort`, `region`, `keyword`, `lowestRoomPrice`, `highestRoomPrice`, `partyType`, `roomType`, `headCountType`, `amenities`, `moods`, `pageNumber`

### 스텝 구인 공고 (StaffRecruitment)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| GET | `/api/v1/staff-recruitment/` | 선택 | 공고 목록 (필터/정렬) |
| GET | `/api/v1/staff-recruitment/recommendation` | 불필요 | 지역별 랜덤 추천 |
| GET | `/api/v1/staff-recruitment/{id}/details` | 선택 | 공고 상세 조회 |
| GET | `/api/v1/staff-recruitment/{id}/questions` | 필요 | 공고 지원 질문 조회 |
| POST | `/api/v1/staff-recruitment/` | 필요 | 공고 등록 |
| GET | `/api/v1/staff-recruitment/owner` | 필요 | 내 공고 목록 |
| PATCH | `/api/v1/staff-recruitment/{id}` | 필요 | 공고 상태 변경 (ACTIVE/INACTIVE) |
| DELETE | `/api/v1/staff-recruitment/{id}` | 필요 | 공고 삭제 |

**조회 필터:** `sort`, `keyword`, `region`, `workType`, `workDays`, `restDays`, `period`, `workScheduleType`, `gender`, `pageNumber`

### 찜 (Wish)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/api/v1/wish/staff-recruitment/{id}` | 필요 | 공고 찜 추가 |
| DELETE | `/api/v1/wish/{id}` | 필요 | 찜 삭제 |

### 이미지 (Image)

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/api/v1/application/images` | 필요 | 단일 이미지 업로드 |
| POST | `/api/v1/images` | 필요 | 다중 이미지 업로드 |

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
| `staff_recruitment` | `StaffRecruitment` | |
| `wish` | `Wish` | |

### User 엔티티 특이사항

```java
@Table(name = "uuser")          // user는 MySQL 예약어라 uuser 사용
@Embedded PersonalInfo          // name, phoneNumber, birthDate, gender를 묶은 값 객체
@Enumerated(EnumType.STRING)
Role role;                      // "사용자" 또는 "운영자" (한글 Enum)
```

`PersonalInfo`는 `@Embeddable`로 별도 테이블 없이 uuser 테이블 컬럼으로 저장됨.

---

## 11. 중요한 설계 결정 및 특이사항

### 지원 시 스냅샷 저장

공고에 지원할 때 **지원서를 JSON으로 직렬화**해서 `application_record.applicationSnapShot` 컬럼에 저장함.

```
지원 시점 지원서 → JSON 문자열로 직렬화 → DB 저장
이후 지원서를 수정해도 지원 당시 내용은 그대로 보존
운영자가 지원자 정보를 조회할 때 스냅샷을 역직렬화해서 반환
```

이 설계 덕분에 지원자가 지원서를 수정해도 운영자는 지원 당시 내용을 볼 수 있음.

---

### 운영자 인증 ↔ Role 변경이 분리됨

`Certificate.decide(isApproved)` 는 `Certificate.status`만 변경하고 `User.role`은 변경하지 않음.

즉, 인증서가 승인돼도 Role은 자동으로 `운영자`로 바뀌지 않음.
운영자 기능(인증서 목록, 지원자 관리 등)은 `userService.validateAdmin(userId)`로 Role을 직접 확인하는 방식으로 보호됨.

프로필에서 "운영자" 배지 표시는 `certificateRepository.existsByUserId(userId)`로 판단 (Role이 아닌 Certificate 존재 여부 기준).

---

### 중복 지원 방지

공고에 중복 지원 시 `existsByStaffRecruitmentIdAndUserId()` 로 체크 후 `IllegalArgumentException` 발생.
(현재 `GuestHouseException` 계층이 아닌 `IllegalArgumentException` 사용 중 — GlobalExceptionHandler가 못 잡아 500 반환될 수 있음)

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

## 12. 요청 처리 흐름

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
