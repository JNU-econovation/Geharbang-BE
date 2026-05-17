
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
12. [요청 처리 흐름](#12-요청-처리-흐름)
13. [인프라 및 실행 환경](#13-인프라-및-실행-환경)
14. [API 문서 (Swagger)](#14-api-문서-swagger)

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
| `certificate` | 사장님 인증서 제출 및 관리자 심사 |
| `guestHousePost` | 게스트하우스 숙소 게시글 CRUD |
| `staffrecruitment` | 게스트하우스 스텝 구인 공고 CRUD |
| `wish` | 스텝 구인 공고 / 게스트하우스 게시글 찜하기 기능 |

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
| `wish` | `Wish` | `staffRecruitmentId` 또는 `guestHousePostId` 중 하나로 찜 대상을 구분 |

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

---

## 13. 인프라 및 실행 환경

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

## 14. API 문서 (Swagger)

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
