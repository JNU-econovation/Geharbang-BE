# Geharbang-BE 학습 가이드

이 문서는 Geharbang-BE 코드를 읽으면서 같이 공부하면 좋은 주제를 정리한 문서다.
단순히 백엔드 기술 키워드를 나열하는 것이 아니라, 실제 프로젝트 코드에서 해당 개념이 어디에 쓰이는지를 기준으로 정리한다.

---

## 1. Spring Boot 계층 구조

### 공부할 것

- Controller, Service, Repository의 역할 분리
- DTO와 Entity를 분리하는 이유
- 요청 검증, 비즈니스 로직, DB 접근이 각각 어느 계층에 있어야 하는지

### 이 프로젝트에서 보는 곳

| 주제 | 코드 |
|------|------|
| 게스트하우스 API 진입점 | `src/main/java/guesthouse/guestHousePost/controller/GuestHousePostController.java` |
| 게스트하우스 비즈니스 로직 | `src/main/java/guesthouse/guestHousePost/service/GuestHousePostService.java` |
| 스텝 공고 API 진입점 | `src/main/java/guesthouse/staffrecruitment/controller/StaffRecruitmentController.java` |
| 스텝 공고 비즈니스 로직 | `src/main/java/guesthouse/staffrecruitment/service/StaffRecruitmentService.java` |
| 사용자/권한 공통 로직 | `src/main/java/guesthouse/user/service/UserService.java` |

### 코드에서 확인할 포인트

- Controller는 HTTP 요청을 받고 Service를 호출한 뒤 응답을 만든다.
- Service는 `@Transactional` 경계 안에서 엔티티 조회, 권한 검증, 저장/삭제를 조합한다.
- Mapper 클래스는 request DTO를 Entity로 바꾸는 변환 책임을 가진다.

### 직접 해볼 것

- `POST /api/v1/guest-houses` 요청이 Controller에서 시작해 어떤 Repository 저장까지 이어지는지 따라가 보기
- `PUT /api/v1/guest-houses/{id}` 수정 API에서 Controller와 Service가 각각 맡는 일을 구분해 보기

---

## 2. JPA Entity와 연관 데이터 저장

### 공부할 것

- Entity, Value Object, Embedded 타입
- ID 기반 연관과 JPA 연관관계의 차이
- `saveAll`, `deleteBy...`, dirty checking
- `@Transactional`이 없으면 변경 감지가 동작하지 않는 이유

### 이 프로젝트에서 보는 곳

| 주제 | 코드 |
|------|------|
| 게스트하우스 본문 수정 | `GuestHousePostService.updateGuestHousePost()` |
| 게스트하우스 이미지/편의시설/파티/객실 저장 | `GuestHousePostService.create()`, `saveParties()`, `saveRooms()` |
| 스텝 공고 직무/이미지/질문 저장 | `StaffRecruitmentService` |
| 공통 생성/수정 시각 | `src/main/java/guesthouse/common/domain/TimeEntity.java` |

### 코드에서 확인할 포인트

- 게스트하우스 수정 API는 전체 교체형 PUT이다.
- 본문 엔티티는 `guestHousePost.update(...)`로 변경하고, 하위 목록은 삭제 후 다시 저장한다.
- 이미지, 편의시설, 파티, 객실은 요청 body에 빠지면 삭제된 것으로 처리된다.

### 직접 해볼 것

- `updateGuestHousePost()`에서 `deleteParties`, `deleteRooms`, 이미지 삭제가 왜 필요한지 설명해 보기
- 하위 목록을 삭제 후 재저장하지 않고 부분 수정으로 바꾸려면 어떤 식별자가 추가로 필요한지 생각해 보기

---

## 3. 트랜잭션과 데이터 일관성

### 공부할 것

- `@Transactional`과 `@Transactional(readOnly = true)`
- 하나의 요청에서 여러 테이블을 수정할 때 rollback이 필요한 이유
- JPA dirty checking

### 이 프로젝트에서 보는 곳

| 주제 | 코드 |
|------|------|
| 여러 테이블 저장 | `GuestHousePostService.create()` |
| 여러 테이블 수정 | `GuestHousePostService.updateGuestHousePost()` |
| 인증서 승인/거부 | `CertificateService.decide()` |
| 유저 개인정보 수정 | `UserService.updatePersonalInfo()` |

### 코드에서 확인할 포인트

- 게스트하우스 등록/수정은 본문, 이미지, 편의시설, 파티, 객실을 함께 처리하므로 하나의 트랜잭션이어야 한다.
- 인증서 승인/거부는 `Certificate` 엔티티를 조회한 뒤 `certificate.decide(isApproved)`로 상태를 바꾼다.

### 직접 해볼 것

- `@Transactional`을 제거하면 어떤 변경이 DB에 반영되지 않을 수 있는지 코드 기준으로 설명해 보기
- 파일 업로드처럼 DB 밖의 작업과 DB 트랜잭션이 함께 있을 때 어떤 문제가 생길 수 있는지 정리하기

---

## 4. 인증, JWT, 커스텀 ArgumentResolver

### 공부할 것

- OAuth2 로그인 흐름
- JWT access token 생성/검증
- Spring MVC ArgumentResolver
- Spring Security 없이 인증을 처리할 때의 장단점

### 이 프로젝트에서 보는 곳

| 주제 | 코드 |
|------|------|
| 카카오/구글 로그인 API | `src/main/java/guesthouse/oauth2/controller/Oauth2Controller.java` |
| 로그인 사용자 생성/조회 | `src/main/java/guesthouse/oauth2/service/AuthService.java` |
| JWT 생성/파싱 | `src/main/java/guesthouse/oauth2/service/TokenProcessor.java` |
| 컨트롤러 userId 주입 어노테이션 | `src/main/java/guesthouse/common/annotation/UserId.java` |
| Swagger에서 `@UserId` 숨김 처리 | `src/main/java/guesthouse/common/config/UserIdParameterCustomizer.java` |

### 코드에서 확인할 포인트

- `TokenProcessor`는 access token과 refresh token의 subject를 `AT`, `RT`로 구분한다.
- Controller는 `@UserId Long userId`만 받고, 토큰 파싱 과정은 공통 처리로 숨긴다.
- `@UserId(required = false)`는 비로그인 사용자도 접근 가능한 공개 조회 API에서 사용된다.

### 직접 해볼 것

- `GET /api/v1/guest-houses`에서 로그인/비로그인일 때 `isWished`가 어떻게 달라지는지 따라가기
- `GET /api/v1/guest-houses/{guestHousePostId}/details`에서 `@UserId(required = false)`가 상세 응답의 `isWished`로 이어지는 흐름 따라가기
- 목록 DTO에서 boolean 필드가 `wished`가 아니라 `isWished`로 직렬화되어야 하는 이유를 확인하기
- 만료된 JWT가 들어오면 어떤 예외 코드가 내려가는지 `TokenProcessor`와 예외 계층에서 확인하기

---

## 5. 권한 모델: 사장님과 시스템 운영자

### 공부할 것

- 인증과 인가의 차이
- 역할 기반 권한과 상태 기반 권한
- 403과 404를 어떤 기준으로 나눌지

### 이 프로젝트에서 보는 곳

| 권한 | 코드 |
|------|------|
| 사장님 인증 상태 확인 | `UserService.validateOwnerStatus()` |
| 시스템 운영자 확인 | `UserService.validateAdmin()` |
| 인증서 제출/승인/거부 | `CertificateService` |
| 게스트하우스 등록/수정 권한 | `GuestHousePostService.create()`, `updateGuestHousePost()` |
| 스텝 공고 등록/수정 권한 | `StaffRecruitmentService` |

### 코드에서 확인할 포인트

- 사장님 기능은 인증서 `Status.승인_완료` 여부로 판단하되, 시스템 운영자(`User.role == 운영자`)도 등록/수정 권한을 통과한다.
- 시스템 운영자 기능은 `User.role == 운영자`인 경우에만 허용된다.
- 수정/삭제/상태 변경은 권한 검증 뒤 본인 게시글인지 `existsByOwnerIdAndId()`로 확인한다.

### 직접 해볼 것

- 사장님 권한이 없고 관리자도 아닌 사용자가 게스트하우스 등록을 요청하면 어느 코드에서 막히는지 따라가기
- 관리자 계정이 인증서 `승인_완료` 없이도 게스트하우스/스텝 공고 등록 권한을 통과하는 흐름 따라가기
- 본인 글이 아닌 게시글을 수정하려는 경우 현재 404로 처리되는 이유를 설명해 보기

---

## 6. Querydsl과 동적 검색 조건

### 공부할 것

- Querydsl Q 클래스
- 동적 where 조건에서 `null`을 반환하는 패턴
- `leftJoin`, `groupBy`, `having`
- 페이지네이션과 정렬

### 이 프로젝트에서 보는 곳

| 주제 | 코드 |
|------|------|
| 게스트하우스 필터 검색 | `src/main/java/guesthouse/guestHousePost/repository/GuestHousePostRepositoryImpl.java` |
| 스텝 공고 필터 검색 | `src/main/java/guesthouse/staffrecruitment/repository/StaffRecruitmentRepositoryImpl.java` |
| Querydsl 설정 | `src/main/java/guesthouse/common/config/QuerydslConfig.java` |

### 코드에서 확인할 포인트

- 검색 조건이 없으면 `BooleanExpression`에서 `null`을 반환하고 Querydsl이 해당 조건을 무시한다.
- 게스트하우스 편의시설 필터는 `groupBy`와 `having`으로 선택한 편의시설 개수와 매칭한다.
- 랜덤 추천은 MySQL의 `RAND()`를 사용한다.

### 직접 해볼 것

- 게스트하우스 목록에서 `region`, `amenities`, `moods` 조건이 각각 어떤 SQL 조건으로 바뀌는지 설명해 보기
- `찜_많은순`이 `wish.guestHousePostId` / `wish.staffRecruitmentId`별 count 서브쿼리로 정렬되는 흐름을 따라가기
- `/api/v1/wish/guest-houses/my`, `/api/v1/wish/staff-recruitment/my`가 `wish` 테이블의 대상 ID를 기준으로 기존 목록 DTO를 재사용하는 흐름을 따라가기

---

## 7. 요청 검증과 Swagger 문서화

### 공부할 것

- Bean Validation: `@NotNull`, `@NotEmpty`, `@Valid`
- nested DTO 검증
- springdoc-openapi와 `@Schema`, `@Operation`
- Swagger schema 이름 충돌

### 이 프로젝트에서 보는 곳

| 주제 | 코드 |
|------|------|
| 게스트하우스 등록/수정 요청 DTO | `src/main/java/guesthouse/guestHousePost/dto/request/GuestHouseCreateRequest.java` |
| 상태 변경 요청 DTO | `src/main/java/guesthouse/guestHousePost/dto/request/ChangeStatusRequest.java` |
| Swagger 기본 설정 | `src/main/java/guesthouse/common/config/OpenApiConfig.java` |
| `@UserId` Swagger 숨김 | `src/main/java/guesthouse/common/config/UserIdParameterCustomizer.java` |

### 코드에서 확인할 포인트

- Controller에서 `@RequestBody @Valid`를 붙여야 DTO 검증이 실행된다.
- 같은 이름의 nested record가 여러 도메인에 있으면 Swagger schema 이름이 충돌할 수 있다.
- 상태 변경 요청은 `status`가 null이면 안 되므로 `@NotNull`이 필요하다.

### 직접 해볼 것

- Swagger에서 `PUT /api/v1/guest-houses/{id}` 요청 schema가 어떤 이름으로 표시되는지 확인하기
- DTO 필수값이 빠졌을 때 현재 어떤 응답이 내려오는지 테스트하고, `MethodArgumentNotValidException` 처리 필요성을 정리하기

---

## 8. 예외 처리 설계

### 공부할 것

- 도메인별 ErrorCode
- 커스텀 예외와 공통 예외 응답
- 예외를 HTTP status로 매핑하는 방식

### 이 프로젝트에서 보는 곳

| 주제 | 코드 |
|------|------|
| 공통 예외 응답 | `src/main/java/guesthouse/common/exception/GlobalExceptionHandler.java` |
| 공통 예외 부모 | `src/main/java/guesthouse/common/exception/GuestHouseException.java` |
| 게스트하우스 예외 | `src/main/java/guesthouse/guestHousePost/exception/GuestHousePostErrorCode.java` |
| 유저 예외 | `src/main/java/guesthouse/user/exception/UserErrorCode.java` |
| 지원 예외 | `src/main/java/guesthouse/application/exception/ApplicationErrorCode.java` |

### 코드에서 확인할 포인트

- `GlobalExceptionHandler`는 현재 `GuestHouseException` 계층만 처리한다.
- 일부 코드에서 `IllegalArgumentException`이 사용되면 공통 응답 형식으로 처리되지 않을 수 있다.
- `getGuestHousePostById()`는 `GuestHousePostException(GuestHousePostErrorCode.NOT_FOUND)`를 사용해 404 도메인 에러로 내려간다.

### 직접 해볼 것

- 새 조회/수정 로직을 추가할 때 `IllegalArgumentException` 대신 도메인 예외를 쓰면 어떤 장점이 있는지 정리하기
- Bean Validation 예외와 JWT 예외가 공통 응답 형식으로 내려가는지 확인하기

---

## 9. 파일 업로드와 정적 파일 서빙

### 공부할 것

- `MultipartFile`
- 서버 로컬 저장과 DB URL 저장 분리
- nginx 정적 파일 서빙
- 파일명 충돌 방지와 확장자 검증

### 이 프로젝트에서 보는 곳

| 주제 | 코드 |
|------|------|
| 지원서 이미지 업로드 | `src/main/java/guesthouse/application/service/ImageService.java` |
| 인증서 파일 업로드 | `src/main/java/guesthouse/certificate/service/CertificateService.java` |
| 파일 URL 응답 | `CertificateService.URL_PREFIX` |
| 업로드 경로 설명 | `ARCHITECTURE.md`의 인프라 및 실행 환경 |

### 코드에서 확인할 포인트

- 인증서 파일은 `jpeg`, `jpg`, `pdf`, `png`만 허용한다.
- 파일은 서버 디렉토리에 저장하고 DB에는 `/files/certifications/...` 같은 URL path만 저장한다.
- 운영 환경에서는 nginx가 해당 파일 경로를 외부로 서빙한다.

### 직접 해볼 것

- 이미지 업로드 API 응답 URL이 DB 저장 값과 nginx 서빙 경로에서 어떻게 이어지는지 그려 보기
- 확장자 검증만으로 충분한지, MIME type 검증이 필요한지 생각해 보기

---

## 10. 인프라, Docker, CI/CD

### 공부할 것

- Docker image와 container의 차이
- docker compose
- GitHub Actions artifact
- self-hosted runner
- Spring profile과 환경변수

### 이 프로젝트에서 보는 곳

| 주제 | 코드/문서 |
|------|-----------|
| 운영 구조 | `ARCHITECTURE.md`의 인프라 및 실행 환경 |
| 협업 배포 규칙 | `COLLABORATION_GUIDE.md`의 빌드 및 배포 |
| GitHub Actions | `.github/workflows/cicd-on-prem.yml` |
| 프로필별 설정 | `src/main/resources/application-*.yml` |
| 테스트 설정 | `src/test/resources/application-test.yml` |

### 코드에서 확인할 포인트

- `dev` push 후 새 코드 반영은 JAR artifact 복사, Docker image 재빌드, `server-dev` 재생성이 모두 성공해야 한다.
- `server-mysql`, `server-nginx`는 애플리케이션 코드 변경만으로 재시작하지 않는다.
- 운영 서버는 `dev` profile, 테스트는 H2 기반 `test` profile을 사용한다.

### 직접 해볼 것

- `docker image inspect server:geharbang`와 `docker exec server-dev stat /server/dev.jar`로 새 코드 반영 여부를 확인해 보기
- Swagger에 새 API가 나타나지만 실제 서버에 없는 경우 어떤 배포 단계가 실패했을지 역추적해 보기
- 공개 목록 API가 운영 DB 데이터 때문에 500이 날 수 있으므로, 배포 후 `pageNumber=0` 목록과 `찜_많은순` 정렬을 실제로 호출해 보기
- 공개 상세 API와 내가 찜한 목록 API도 함께 호출해서 `isWished`, `401 LOGIN_REQUIRED`, `404 미반영` 여부를 구분해 보기
- 한글 query string은 curl에서 `--data-urlencode 'sort=찜_많은순'`처럼 인코딩해서 호출해 보기

---

## 11. 테스트 전략

### 공부할 것

- Spring Boot 통합 테스트
- H2와 MySQL 차이
- Service 단위 테스트와 Repository 테스트
- 권한/예외/트랜잭션 테스트

### 이 프로젝트에서 보는 곳

| 주제 | 코드 |
|------|------|
| 현재 테스트 진입점 | `src/test/java/guesthouse/guesthouse/GuestHouseApplicationTests.java` |
| 테스트 profile | `src/test/resources/application-test.yml` |

### 직접 해볼 것

- `GuestHousePostService.updateGuestHousePost()`에 대해 다음 케이스를 테스트로 작성해 보기
- 승인된 사장님이 본인 글을 수정하면 성공
- 승인되지 않은 사용자는 403 성격의 예외
- 본인 글이 아니면 not found 예외
- 수정 요청에서 하위 목록이 바뀌면 기존 목록이 삭제되고 새로 저장됨

---

## 추천 학습 순서

1. Controller → Service → Repository 요청 흐름 읽기
2. JWT와 `@UserId` 인증 흐름 이해
3. 사장님/운영자 권한 모델 이해
4. JPA 트랜잭션과 전체 교체형 수정 API 이해
5. Querydsl 필터 검색 이해
6. 예외 처리와 Swagger 문서화 보강
7. Docker/CI/CD로 운영 반영 흐름 확인
