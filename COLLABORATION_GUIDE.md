
# Geharbang-BE 협업 가이드라인

이 문서는 새로운 팀원이 합류하거나 기존 팀원이 코드를 추가할 때 일관된 코드 스타일을 유지하기 위한 가이드라인이다.
전체 구조와 도메인 설명은 [ARCHITECTURE.md](./ARCHITECTURE.md)를 참고.

---

## 목차

1. [브랜치 전략](#1-브랜치-전략)
2. [새 기능 추가 순서](#2-새-기능-추가-순서)
3. [패키지 및 클래스 네이밍](#3-패키지-및-클래스-네이밍)
4. [레이어별 코드 작성 규칙](#4-레이어별-코드-작성-규칙)
   - [Controller](#41-controller)
   - [Service](#42-service)
   - [Repository](#43-repository)
   - [Entity / Value Object](#44-entity--value-object)
   - [DTO (Request / Response)](#45-dto-request--response)
   - [Mapper](#46-mapper)
   - [Exception / ErrorCode](#47-exception--errorcode)
5. [Enum 정의 규칙](#5-enum-정의-규칙)
6. [검증(Validation) 규칙](#6-검증validation-규칙)
7. [의존성 주입 규칙](#7-의존성-주입-규칙)
8. [트랜잭션 규칙](#8-트랜잭션-규칙)
9. [인증 처리 (@UserId)](#9-인증-처리-userid)
10. [QueryDSL 작성 규칙](#10-querydsl-작성-규칙)
11. [Swagger 문서화](#11-swagger-문서화)
12. [인프라 및 환경 설정](#12-인프라-및-환경-설정)
13. [빌드 및 배포](#13-빌드-및-배포)
14. [금지 사항 (하지 말 것)](#14-금지-사항-하지-말-것)

---

## 1. 브랜치 전략

```
main       ← 배포 가능한 상태만 유지
  └── dev  ← 개발 통합 브랜치 (기본 작업 브랜치)
        └── feature/{기능명}  ← 기능 개발 (선택)
```

- 일상적인 작업은 `dev` 브랜치에 직접 커밋
- 큰 기능이나 실험적인 작업은 `feature/` 브랜치를 따서 PR로 머지
- `main`에는 직접 push 금지

### 커밋 메시지 형식

```
feat: 스텝 공고 필터 API 추가
fix: isOwner 오타 수정
refactor: StaffRecruitmentMapper 분리
chore: springdoc 의존성 추가
```

- `feat` — 새 기능
- `fix` — 버그 수정
- `refactor` — 동작 변화 없는 코드 개선
- `chore` — 빌드, 의존성 등 설정 변경

---

## 2. 새 기능 추가 순서

도메인 하나를 새로 만들 때는 아래 순서를 따른다.

```
1. domain/vo/   → Enum 및 값 객체 정의
2. domain/model/ → JPA Entity 작성 (TimeEntity 상속)
3. exception/   → ErrorCode enum + Exception 클래스
4. dto/request/ → Request DTO (Record, 검증 어노테이션 포함)
5. dto/response/ → Response DTO (Record, from() 메서드 포함)
6. repository/  → JPA Repository 인터페이스
               → 복잡한 조회가 있으면 CustomRepository + Impl 추가
7. mapper/      → Entity ↔ DTO 변환 (stateless utility class)
8. service/     → 비즈니스 로직 (@Transactional)
9. controller/  → HTTP 엔드포인트
```

---

## 3. 패키지 및 클래스 네이밍

### 패키지 구조 (도메인 하나의 예시)

```
guesthouse/
└── {domainName}/
    ├── controller/
    │   └── {Domain}Controller.java
    ├── service/
    │   └── {Domain}Service.java
    ├── repository/
    │   ├── {Domain}Repository.java
    │   ├── {Domain}CustomRepository.java   (복잡한 쿼리 있을 때만)
    │   └── {Domain}RepositoryImpl.java     (QueryDSL 구현체)
    ├── domain/
    │   ├── model/
    │   │   └── {Domain}.java               (JPA Entity)
    │   └── vo/
    │       └── {EnumName}.java             (Enum, Embeddable)
    ├── dto/
    │   ├── {Domain}DTO.java                (레이어 간 전달용 내부 DTO)
    │   ├── request/
    │   │   └── {Domain}CreateRequest.java
    │   └── response/
    │       └── {Domain}Response.java
    ├── mapper/
    │   └── {Domain}Mapper.java
    └── exception/
        ├── {Domain}ErrorCode.java
        └── {Domain}Exception.java
```

### 클래스명 규칙

| 유형 | 형식 | 예시 |
|------|------|------|
| Entity | `{Domain}` | `StaffRecruitment`, `GuestHousePost` |
| Request DTO | `{Domain}{Action}Request` | `StaffRecruitmentCreateRequest` |
| Response DTO | `{Domain}Response` 또는 `{Domain}DTO` | `StaffRecruitmentDetailsResponse` |
| 내부 DTO | `{Domain}DTO` | `StaffRecruitmentDetailsDTO` |
| Service | `{Domain}Service` | `StaffRecruitmentService` |
| Controller | `{Domain}Controller` | `StaffRecruitmentController` |
| Repository | `{Domain}Repository` | `StaffRecruitmentRepository` |
| Mapper | `{Domain}Mapper` | `StaffRecruitmentMapper` |
| ErrorCode | `{Domain}ErrorCode` | `StaffRecruitmentErrorCode` |
| Exception | `{Domain}Exception` | `StaffRecruitmentException` |
| Enum (VO) | `{EnumName}` | `Gender`, `WorkType`, `Region` |

---

## 4. 레이어별 코드 작성 규칙

### 4.1 Controller

**기본 골격:**

```java
@RestController
@RequestMapping("/api/v1/{resource}")
@RequiredArgsConstructor
public class StaffRecruitmentController {

    private final StaffRecruitmentService staffRecruitmentService;

    // 비로그인 허용: @UserId(required = false)
    @GetMapping("/{id}/details")
    public ResponseEntity<StaffRecruitmentDetailsResponse> getDetails(
            @PathVariable Long id,
            @UserId(required = false) Long userId
    ) {
        StaffRecruitmentDetailsDTO details = staffRecruitmentService.getDetails(id, userId);
        return ResponseEntity.ok(StaffRecruitmentDetailsResponse.from(details));
    }

    // 로그인 필수: @UserId
    @PostMapping
    public ResponseEntity<StaffRecruitmentIdResponse> create(
            @UserId Long userId,
            @RequestBody @Valid StaffRecruitmentCreateRequest request
    ) {
        Long id = staffRecruitmentService.createStaffRecruitment(userId, request);
        return ResponseEntity.ok(new StaffRecruitmentIdResponse(id));
    }

    // 삭제/void 응답
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @UserId Long userId,
            @PathVariable Long id
    ) {
        staffRecruitmentService.delete(userId, id);
        return ResponseEntity.ok().build();
    }
}
```

**규칙:**
- 반환 타입은 반드시 `ResponseEntity<T>` 사용
- Controller는 서비스 호출 + 응답 변환만 담당. 비즈니스 로직 절대 금지
- 로그인 필요한 엔드포인트는 `@UserId Long userId`, 선택적 로그인은 `@UserId(required = false) Long userId`
- `@RequestBody`에는 반드시 `@Valid` 붙이기
- `@PathVariable`의 변수명이 메서드 파라미터명과 다를 경우 `@PathVariable("id") Long recruitmentId`처럼 명시

---

### 4.2 Service

**기본 골격:**

```java
@Service
@RequiredArgsConstructor
public class StaffRecruitmentService {

    private final StaffRecruitmentRepository staffRecruitmentRepository;
    private final UserService userService;

    // 조회 — readOnly = true
    @Transactional(readOnly = true)
    public StaffRecruitmentDetailsDTO getDetails(Long id, Long userId) {
        StaffRecruitment recruitment = getStaffRecruitmentById(id);
        // ...
        return StaffRecruitmentDetailsDTO.from(recruitment, ...);
    }

    // 등록/수정/삭제 — 기본 @Transactional
    @Transactional
    public Long createStaffRecruitment(Long userId, StaffRecruitmentCreateRequest request) {
        userService.validateOwnerStatus(userId);  // 권한 확인은 첫 줄에
        StaffRecruitment entity = StaffRecruitmentMapper.from(request, userId);
        staffRecruitmentRepository.save(entity);
        return entity.getId();
    }

    // 내부 헬퍼 — private으로
    private StaffRecruitment getStaffRecruitmentById(Long id) {
        return staffRecruitmentRepository.findById(id)
                .orElseThrow(() -> new StaffRecruitmentException(StaffRecruitmentErrorCode.NOT_FOUND));
    }
}
```

**규칙:**
- 조회 메서드는 `@Transactional(readOnly = true)`, 쓰기 메서드는 `@Transactional`
- 권한 검증은 메서드 첫 줄에 처리 (`userService.validateOwnerStatus(userId)`)
- 게스트하우스/스텝 공고 등록·수정 API는 반드시 `validateOwnerStatus(userId)`로 `승인_완료` 사장님 또는 시스템 운영자 권한을 확인
- 게시글/공고의 수정·상태 변경·삭제처럼 특정 리소스를 바꾸는 API는 `existsByOwnerIdAndId(userId, id)`로 본인 소유 여부도 확인
- 엔티티 조회 후 없으면 바로 예외 throw. `Optional`을 상위로 올리지 말 것
- 공통으로 쓰는 엔티티 조회 로직은 `private` 헬퍼 메서드로 추출
- 서비스 간 의존은 허용하되 순환 참조 주의

---

### 4.3 Repository

**기본 구조 (단순 쿼리만 있을 때):**

```java
@Repository
public interface StaffRecruitmentRepository extends JpaRepository<StaffRecruitment, Long> {
    List<StaffRecruitment> findByOwnerId(Long ownerId);
    boolean existsByOwnerIdAndId(Long ownerId, Long id);
}
```

**복잡한 쿼리가 있을 때 — Custom Repository 패턴:**

```java
// 1. 인터페이스 분리
public interface StaffRecruitmentCustomRepository {
    List<StaffRecruitment> searchByFilter(Pageable pageable, StaffRecruitmentFilter filter);
}

// 2. 메인 Repository가 확장
@Repository
public interface StaffRecruitmentRepository
        extends JpaRepository<StaffRecruitment, Long>, StaffRecruitmentCustomRepository {
    // 단순 JPA 메서드 선언
}

// 3. QueryDSL Impl
@RequiredArgsConstructor
public class StaffRecruitmentRepositoryImpl implements StaffRecruitmentCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<StaffRecruitment> searchByFilter(Pageable pageable, StaffRecruitmentFilter filter) {
        return jpaQueryFactory.selectDistinct(staffRecruitment)
                .from(staffRecruitment)
                .where(
                        regionIn(filter.getRegion()),
                        isActive()
                )
                .orderBy(staffRecruitment.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    // BooleanExpression 헬퍼 — null 반환 시 where 절에서 자동 무시됨
    private BooleanExpression regionIn(List<Region> regions) {
        if (regions == null || regions.isEmpty()) return null;
        return staffRecruitment.region.in(regions);
    }

    private BooleanExpression isActive() {
        return staffRecruitment.status.eq(Status.ACTIVE);
    }
}
```

**규칙:**
- 단순 조회 (`findBy`, `existsBy`)는 JPA 메서드 이름 쿼리로
- 필터/페이지네이션/정렬이 복잡하면 QueryDSL Impl 분리
- `RepositoryImpl` 클래스명은 반드시 `{Repository명}Impl` 형식 유지 (Spring Data가 자동 감지)
- QueryDSL where 조건은 각각 `private BooleanExpression` 메서드로 분리

---

### 4.4 Entity / Value Object

**Entity 기본 골격:**

```java
@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StaffRecruitment extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ownerId;     // FK 대신 Long 타입으로 ID만 저장 (이 프로젝트 방식)

    @Enumerated(EnumType.STRING)
    private Region region;

    @Column(columnDefinition = "TEXT")
    private String content;   // 긴 문자열은 TEXT 명시

    // 비즈니스 메서드 — setter 대신 의미 있는 메서드명 사용
    public void plusViewCount() {
        this.viewCount += 1;
    }

    public void changeStatus(Status status) {
        this.status = status;
    }
}
```

**Embeddable Value Object:**

```java
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Contact {

    private String phoneNumber;
    private String instagramId;
    private String webSite;

    // 모든 필드를 받는 생성자를 직접 정의 (@AllArgsConstructor 대신)
    public Contact(String phoneNumber, String instagramId, String webSite) {
        this.phoneNumber = phoneNumber;
        this.instagramId = instagramId;
        this.webSite = webSite;
    }
}
```

**규칙:**
- Entity는 반드시 `TimeEntity` 상속 (createdAt, updatedAt 자동 관리)
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` — JPA용 기본 생성자를 protected로 제한
- **setter 금지** — 상태 변경은 의미 있는 메서드명으로 (`changeStatus()`, `plusViewCount()`)
- Enum 필드는 반드시 `@Enumerated(EnumType.STRING)` 명시
- 이 프로젝트는 FK 대신 연관 엔티티의 ID를 Long 타입으로 직접 저장하는 방식을 사용
- 긴 문자열 컬럼은 `@Column(columnDefinition = "TEXT")` 명시

---

### 4.5 DTO (Request / Response)

**Request DTO — Record + 검증 어노테이션:**

```java
public record StaffRecruitmentCreateRequest(

        @NotBlank
        @Length(min = 5, max = 30)
        String title,

        @NotNull
        Region region,

        @Valid
        @NotNull
        WorkingInformation workingInformation,

        @Size(max = 10)
        List<String> questions

) {
    // 중첩 record — Request 내부에 같이 정의
    public record WorkingInformation(

            @NotNull
            LocalDate startDate,

            @NotEmpty
            @Valid
            List<Job> jobs

    ) { }

    public record Job(
            @NotBlank
            @Size(max = 20)
            String name,

            @NotNull
            WorkType standard,

            Integer workDays,  // nullable 필드는 @NotNull 없이
            Integer restDays

    ) { }
}
```

**Response DTO — Record + from() 정적 팩토리:**

```java
@Builder
public record StaffRecruitmentDetailsResponse(
        String title,
        String guestHouseName,
        WorkingInformation workingInformation,
        Boolean isWished
) {
    // DTO → Response 변환 (Service 내부 DTO를 받아 변환)
    public static StaffRecruitmentDetailsResponse from(StaffRecruitmentDetailsDTO dto) {
        return StaffRecruitmentDetailsResponse.builder()
                .title(dto.title())
                .guestHouseName(dto.guestHouseName())
                .workingInformation(WorkingInformation.from(dto.jobs()))
                .isWished(dto.isWished())
                .build();
    }

    // 중첩 Response record도 from() 패턴 사용
    public static record WorkingInformation(List<JobSummaryDTO> jobs) {
        public static WorkingInformation from(List<StaffRecruitmentJobDTO> jobs) {
            return new WorkingInformation(jobs.stream().map(JobSummaryDTO::from).toList());
        }
    }
}
```

**규칙:**
- Request/Response DTO는 모두 **Record** 사용
- Request는 `dto/request/`, Response는 `dto/response/` 패키지에 위치
- Request 중첩 구조는 Record 내부에 중첩 Record로 정의
- Response는 `from()` 또는 `of()` 정적 팩토리로 변환 (직접 new 생성 지양)
- Response에 `@Builder` 붙이면 빌더 패턴으로 생성 가능

---

### 4.6 Mapper

Entity ↔ DTO 변환 로직을 담당. **인스턴스화 불가능한 유틸리티 클래스**로 작성.

```java
public final class StaffRecruitmentMapper {

    // 자주 쓰는 구분자는 상수로
    public static final String DELIMITER = "|:|";

    private StaffRecruitmentMapper() {  // 인스턴스화 방지
    }

    // Request → Entity
    public static StaffRecruitment from(StaffRecruitmentCreateRequest request, Long ownerId) {
        return StaffRecruitment.builder()
                .ownerId(ownerId)
                .title(request.title())
                .region(request.region())
                // ...
                .status(Status.ACTIVE)
                .build();
    }

    // 리스트 변환 등 보조 메서드도 여기에
    private static String joinList(List<String> list) {
        return Optional.ofNullable(list)
                .map(l -> String.join(DELIMITER, l))
                .orElse("");
    }
}
```

**규칙:**
- `public final class` + `private 생성자`로 선언
- 모든 메서드는 `static`
- `from()` — Request → Entity 변환
- `of()` — Entity → DTO 변환 (선택)
- Entity에 null이 들어가면 안 되는 필드는 `Objects.requireNonNullElse()` 또는 `Optional`로 처리

---

### 4.7 Exception / ErrorCode

**ErrorCode Enum:**

```java
public enum StaffRecruitmentErrorCode implements ErrorCode {

    NOT_FOUND("스태프 모집글이 존재하지 않습니다.", 404),
    NOT_OWNER("본인 게시글이 아닙니다.", 403),
    INVALID_STATUS("잘못된 상태값입니다."),  // HTTP 상태 생략 시 400 기본값
    ;

    private final String message;
    private final int statusCode;

    StaffRecruitmentErrorCode(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    StaffRecruitmentErrorCode(String message) {
        this(message, 400);
    }

    @Override public String getMessage()   { return message; }
    @Override public int getStatus()       { return statusCode; }
    @Override public String getErrorCode() { return this.name(); }
}
```

**Exception 클래스:**

```java
public class StaffRecruitmentException extends GuestHouseException {
    public StaffRecruitmentException(ErrorCode errorCode) {
        super(errorCode);
    }
}
```

**사용법:**

```java
// Service 또는 Repository 내부에서
staffRecruitmentRepository.findById(id)
        .orElseThrow(() -> new StaffRecruitmentException(StaffRecruitmentErrorCode.NOT_FOUND));

if (!existsByOwnerIdAndId(userId, id))
    throw new StaffRecruitmentException(StaffRecruitmentErrorCode.NOT_OWNER);
```

**규칙:**
- 도메인마다 `{Domain}ErrorCode` enum + `{Domain}Exception` 클래스 한 쌍 생성
- 에러 메시지는 한국어로 작성 (FE에 직접 노출될 수 있음)
- HTTP 상태코드 생략 시 400이 기본값 — 의도적으로 기본값이면 생략해도 됨
- `GlobalExceptionHandler`는 건드리지 않음. 새 도메인 예외를 추가해도 자동 처리됨

---

## 5. Enum 정의 규칙

이 프로젝트는 Enum 값을 **한글**과 **영문** 혼용으로 정의함.

```java
// 한글 Enum — DB에도 한글로 저장됨
public enum Region {
    제주시, 애월, 한림, 대정, 서귀포시, 남원, 표선, 성산, 구좌, 조천, 한경
}

// 영문 Enum — UPPER_CASE
public enum Status {
    ACTIVE, INACTIVE;

    // Enum에 비즈니스 메서드 추가 가능
    public boolean isClosed() {
        return this == INACTIVE;
    }
}

// 한글 Enum — @Getter + @AllArgsConstructor로 필드 포함
@AllArgsConstructor
@Getter
public enum Gender {
    남, 여, 무관
}
```

**규칙:**
- 모든 Enum 필드는 Entity에서 `@Enumerated(EnumType.STRING)` 사용 (ordinal 금지)
- Enum에 간단한 비즈니스 메서드를 넣는 것은 허용 (`isClosed()`, `isActive()` 등)
- 새 Enum 값 추가 시 DB 마이그레이션 불필요 (EnumType.STRING이라 문자열로 저장됨)

---

## 6. 검증(Validation) 규칙

Request DTO의 필드에 어노테이션으로 선언.

```java
public record GuestHouseCreateRequest(

        @NotBlank                    // null, "", " " 모두 거부
        @Length(min = 2, max = 30)   // 문자열 길이 (Hibernate validator)
        String guestHouseName,

        @NotNull                     // null만 거부 (빈 문자열은 통과)
        Region region,

        @Valid                       // 중첩 객체의 검증을 전파
        @NotNull
        Location location,

        @NotEmpty                    // null + 빈 컬렉션 거부
        @Size(min = 1, max = 10)     // 컬렉션 크기
        List<String> imageUrls,

        @Size(min = 1, max = 10)
        List<@Length(min = 1, max = 20) String> amenities,  // 컬렉션 원소 검증

        @PositiveOrZero              // 0 이상 정수
        Integer pricePerNight

) { }
```

**규칙:**
- 문자열 길이: `@Length` (Hibernate) 사용
- 컬렉션 크기: `@Size` (Jakarta) 사용
- 필수 컬렉션은 `@Size`만 쓰지 말고 `@NotEmpty` 또는 `@NotNull`을 함께 사용. `@Size`만 있으면 `null`은 통과함
- 필수 중첩 객체와 좌표 리스트처럼 서비스에서 바로 사용하는 값은 `@NotNull`을 붙여 500이 아닌 400으로 실패하게 만들기
- 중첩 Record 검증 전파: `@Valid` 필수
- Controller의 `@RequestBody`에 반드시 `@Valid` 붙이기 — 없으면 검증 동작 안 함
- 검증 실패 시 `GlobalExceptionHandler`가 자동으로 400 응답 처리

**수정 API 주의:**
- 현재 게스트하우스/스텝 공고 수정은 전체 교체형 `PUT` 방식이다.
- 하위 목록(이미지, 편의시설, 파티, 객실, 직무, 추가 질문 등)은 요청 body 기준으로 삭제 후 재저장한다.
- 프론트가 기존 데이터를 유지하려면 수정 요청 body에 유지할 항목도 모두 포함해야 한다.
- `@ElementCollection` 필드는 컬렉션 참조를 통째로 바꾸기보다 기존 컬렉션을 `clear()` 후 `addAll()` 하는 방식이 안전하다.

---

## 7. 의존성 주입 규칙

```java
// 생성자 주입 — Lombok @RequiredArgsConstructor 사용
@Service
@RequiredArgsConstructor
public class StaffRecruitmentService {

    private final StaffRecruitmentRepository staffRecruitmentRepository;
    private final UserService userService;
    // final 필드만 선언 → @RequiredArgsConstructor가 생성자 자동 생성
}
```

**규칙:**
- `@Autowired` 필드 주입 금지
- `@RequiredArgsConstructor` + `private final` 필드 패턴만 사용
- Mapper는 static 메서드만 있으므로 주입 불필요 (`StaffRecruitmentMapper.from(...)` 직접 호출)

---

## 8. 트랜잭션 규칙

```java
// 조회 전용 — readOnly = true (성능 최적화, 변경 감지 비활성화)
@Transactional(readOnly = true)
public StaffRecruitmentDetailsDTO getDetails(Long id, Long userId) { ... }

// 생성/수정/삭제 — 기본 @Transactional
@Transactional
public Long create(Long userId, StaffRecruitmentCreateRequest request) { ... }

// @Transactional 없으면: 단순 조회 + 트랜잭션 불필요한 메서드
public boolean existsByOwnerIdAndId(Long userId, Long id) {
    return staffRecruitmentRepository.existsByOwnerIdAndId(userId, id);
}
```

**규칙:**
- DB 쓰기가 있는 메서드: `@Transactional`
- 읽기 전용 메서드: `@Transactional(readOnly = true)`
- 단순 boolean 체크 등 쓰기 없는 메서드: 트랜잭션 없어도 됨
- 트랜잭션 안에서 여러 엔티티를 저장할 때는 순서 주의 (먼저 저장된 엔티티의 ID가 필요한 경우)

---

## 9. 인증 처리 (@UserId)

```java
// 로그인 필수 — 토큰 없으면 자동으로 401 반환
@PostMapping
public ResponseEntity<?> create(@UserId Long userId, @RequestBody @Valid Request request) { ... }

// 로그인 선택 — 토큰 없으면 userId = null
@GetMapping("/{id}")
public ResponseEntity<?> get(@PathVariable Long id, @UserId(required = false) Long userId) { ... }
```

**언제 required = false를 쓰는가:**
- 비로그인 유저도 조회할 수 있지만, 로그인한 경우 추가 정보(찜 여부 등)를 내려야 할 때
- 게스트하우스/스텝 공고 목록과 상세처럼 비회원 조회는 허용하되 로그인 유저에게 `isWished`를 내려야 할 때

**규칙:**
- 로그인이 필요한 API는 `@UserId Long userId` (required = true가 기본값)
- 조회 API 중 비로그인도 허용할 때만 `@UserId(required = false) Long userId`
- `isWished`가 필요한 공개 조회 API는 별도 찜 확인 API를 추가하지 말고 기존 응답 DTO에 `isWished`를 포함한다
- 마이페이지처럼 “내가 찜한 목록” 자체가 필요한 경우에만 `/api/v1/wish/.../my` 조회 API를 둔다
- Spring Security 없음 — 인증 처리는 오직 `UserIdResolver`에서만

---

## 10. QueryDSL 작성 규칙

```java
public class StaffRecruitmentRepositoryImpl implements StaffRecruitmentCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<StaffRecruitment> searchByFilter(Pageable pageable, StaffRecruitmentFilter filter) {
        return jpaQueryFactory
                .selectDistinct(staffRecruitment)  // Q 클래스는 static import
                .from(staffRecruitment)
                .leftJoin(staffRecruitmentJob)
                    .on(staffRecruitmentJob.staffRecruitmentId.eq(staffRecruitment.id))
                .where(
                        regionIn(filter.getRegion()),   // null 반환 시 자동 무시
                        genderEq(filter.getGender()),
                        isActive()
                )
                .orderBy(staffRecruitment.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    // BooleanExpression 메서드로 분리 — null 반환 가능
    private BooleanExpression regionIn(List<Region> regions) {
        if (regions == null || regions.isEmpty()) return null;
        return staffRecruitment.region.in(regions);
    }

    private BooleanExpression isActive() {
        return staffRecruitment.status.eq(Status.ACTIVE);
    }
}
```

**규칙:**
- Q 클래스는 `src/main/generated/` 디렉토리에 자동 생성됨 (compileJava 후)
- 각 필터 조건은 `private BooleanExpression` 메서드로 분리 (null 반환 가능 → where에서 무시)
- 랜덤 정렬: `Expressions.numberTemplate(Double.class, "RAND()").asc()` 사용
- RAND() 등 DB 함수는 QueryDSL의 `Expressions.numberTemplate()` 사용

---

## 11. Swagger 문서화

별도 설정 없이 `@RestController` + DTO 필드만 있으면 자동으로 Swagger 문서가 생성됨.

**접속 URL:** `https://geharbang.org/swagger-ui/index.html`

**필요할 때만 추가 어노테이션:**

```java
// Response DTO 필드에 설명이 필요한 경우
public record ProfileResponse(
        Boolean isOwner,

        @Schema(description = "User.role == 운영자인 경우 true", example = "false")
        Boolean isAdmin,

        @Schema(description = "가장 최근 인증서 상태. 인증서 없으면 null", example = "거부됨")
        String certificateStatus
) { }
```

**규칙:**
- `@UserId` 파라미터는 `UserIdParameterCustomizer`가 자동으로 Swagger에서 숨김 처리 — 별도 작업 불필요
- 대부분의 필드는 어노테이션 없이 자동 문서화됨. 의미가 불명확한 필드에만 `@Schema` 추가
- 서로 다른 도메인에 같은 이름의 Request DTO나 nested record가 있으면 Swagger component schema 이름이 충돌할 수 있음
- 이름이 겹치는 DTO는 `@Schema(name = "GuestHouseLocation")`처럼 도메인 prefix를 붙여 schema 이름을 분리
- 상태 변경 요청처럼 같은 record 이름이 반복되기 쉬운 DTO는 `GuestHouseChangeStatusRequest`, `StaffRecruitmentChangeStatusRequest`처럼 명확한 Swagger 이름을 지정

---

## 12. 인프라 및 환경 설정

자세한 구성도는 [ARCHITECTURE.md - 인프라 및 실행 환경](./ARCHITECTURE.md#13-인프라-및-실행-환경)을 참고.
이 섹션은 개발/배포 중 실수하기 쉬운 운영 규칙을 정리한다.

### 실행 환경 요약

| 환경 | Spring profile | 실행 방식 | DB |
|------|----------------|----------|----|
| 로컬 개발 | `local` | IDE 또는 `./gradlew bootRun` | MySQL |
| 테스트 | `test` | `./gradlew test` | H2 in-memory |
| 서버 배포 | `dev` | Docker `server-dev` 컨테이너 | Docker MySQL |

`application.yml`은 기본 profile을 `local`로 둔다.
테스트 리소스의 `application.yml`은 기본 profile을 `test`로 둔다.
서버 컨테이너는 `server-dockerfile`에서 `-Dspring.profiles.active=dev`로 실행된다.

### 로컬 환경 변수

루트의 `.env` 파일은 `application.yml`의 `spring.config.import=optional:file:.env[.properties]`로 읽힌다.
민감값은 Git에 올리지 않는다.

필수 값:

| 변수 | 설명 |
|------|------|
| `DB_URL` | MySQL JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | DB 계정 |
| `JWT_SECRET_KEY` | JWT 서명 키 |
| `JWT_ACCESS_EXPIRATION_TIME` | Access Token 만료 시간 (ms) |
| `JWT_REFRESH_EXPIRATION_TIME` | Refresh Token 만료 시간 (ms) |
| `KAKAO_CLIENT_ID`, `KAKAO_REDIRECT_URI` | 카카오 OAuth2 |
| `GOOGLE_CLIENT_ID`, `GOOGLE_SECRET_KEY`, `GOOGLE_REDIRECT_URI` | 구글 OAuth2 |
| `BASE_REDIRECT_URI` | 로그인 후 프론트 리다이렉트 기본 URL |
| `IMAGE_DIRECTORY_PATH` | 이미지 저장 경로 |
| `CERTIFICATE_DIRECTORY_PATH` | 인증서 저장 경로 |
| `SENTRY_DSN` | dev profile Sentry 전송 DSN |

### DB 규칙

- 운영/로컬 개발 DB는 MySQL을 기준으로 작성한다.
- 테스트는 H2를 사용하므로 MySQL 전용 SQL이나 예약어 사용 시 테스트와 운영 동작이 달라질 수 있다.
- 운영 서버 DB 컨테이너 이름은 `server-mysql`, Docker network는 `guesthouse`다.
- 서버 DB 데이터는 Docker volume이 아니라 호스트 디렉토리 `./db/data`에 bind mount된다.
- Entity 변경 시 `ddl-auto: update`가 자동 반영하지만, 데이터 보존이 필요한 변경은 직접 마이그레이션 계획을 세운다.
- `user`, `value`처럼 DB 예약어 가능성이 있는 이름은 피한다. 이미 `User` 엔티티는 `uuser` 테이블명을 사용한다.

### Docker 운영 규칙

서버는 `/Users/brains/guesthouse/jeju` 아래의 compose 파일들로 운영된다.

| 파일 | 역할 |
|------|------|
| `server-docker-compose.yml` | `server-dev` Spring Boot 컨테이너 |
| `mysql-docker-compose.yml` | `server-mysql` MySQL 컨테이너 |
| `nginx-docker-compose.yml` | `server-nginx` nginx 컨테이너 |
| `server-dockerfile` | Java 21 기반 서버 이미지 |
| `deploy.sh` | 서버 이미지 빌드 및 컨테이너 재생성 |

운영 중 주의:

- 애플리케이션만 재배포할 때는 `server-dev`만 재생성한다.
- DB 작업이 아닌데 `server-mysql` 컨테이너나 `./db/data` 디렉토리를 삭제하지 않는다.
- nginx 설정 변경 시에는 `server-nginx` 재시작 전 설정 파일 문법을 확인한다.
- 세 컨테이너는 모두 `guesthouse` Docker network에 있어야 한다.
- Mac mini self-hosted runner에서는 Docker credential helper가 기본 PATH에 없을 수 있으므로 `deploy.sh`에서 `/Applications/Docker.app/Contents/Resources/bin`을 PATH에 추가한다.
- `server-dev`가 재시작됐다는 사실만으로 새 코드 반영을 판단하지 않는다. 새 image 생성 시각과 API 동작을 함께 확인한다.

### 파일 업로드 경로

서버 컨테이너와 nginx 컨테이너는 업로드 디렉토리를 공유한다.

| 종류 | Spring Boot 컨테이너 | nginx 컨테이너 |
|------|---------------------|----------------|
| 이미지 | `/home/geharbang/images` | `/home/jeju/images` |
| 인증서 | `/home/geharbang/certs` | `/home/jeju/certs` |

코드에서는 `IMAGE_DIRECTORY_PATH`, `CERTIFICATE_DIRECTORY_PATH`만 사용한다.
외부 URL은 `/images/{fileName}`, `/files/certifications/{fileName}` 형태로 저장/응답한다.

---

## 13. 빌드 및 배포

### 로컬 빌드

```bash
# 테스트 제외 빌드 (dev 서버 배포 시 기본)
./gradlew bootJar -x test

# clean 빌드 (의존성 변경 등 확실히 새로 빌드하고 싶을 때)
./gradlew clean bootJar -x test
```

### 서버 배포 (수동 배포)

```bash
# 1. 빌드
./gradlew clean bootJar -x test

# 2. JAR 복사
docker cp build/libs/dev.jar server-dev:/server/dev.jar

# 3. 서버 재시작
docker restart server-dev

# 4. 로그 확인 (시작 완료 메시지: "Started GuestHouseApplication in X.X seconds")
docker logs server-dev --tail 30
```

### CI/CD 자동 배포

`dev` 브랜치에 push하면 GitHub Actions가 자동으로 빌드 + 배포.  
단, `build.gradle` 의존성 추가 등 중요한 변경은 **push 전에 로컬 빌드로 컴파일 오류 먼저 확인**.
자동 배포는 새 JAR artifact를 서버 작업 디렉토리에 복사한 뒤, Docker image를 다시 빌드하고 `server-dev` 컨테이너를 재생성하는 구조다.
따라서 push 이후에는 GitHub Actions 성공 여부와 운영 서버의 실제 API 동작을 함께 확인한다.

배포 스크립트는 아래 조건을 만족해야 한다.

```sh
#!/bin/sh
set -eu

export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"

docker build --no-cache -t server:geharbang -f ./server-dockerfile .
docker compose -f ./server-docker-compose.yml up -d --force-recreate
```

**배포 후 확인:**

```bash
# 1. 컨테이너 기동 확인
docker ps --filter name=server-dev

# 2. 서버 health 확인
curl -s http://localhost:8080/actuator/health

# 3. 공개 목록 API가 운영 데이터로 200을 반환하는지 확인
curl -i "http://localhost:8080/api/v1/guest-houses?pageNumber=0"
curl -i "http://localhost:8080/api/v1/staff-recruitment?pageNumber=0"

# 4. 공개 상세 API가 200을 반환하고 isWished 필드를 포함하는지 확인
curl -i "http://localhost:8080/api/v1/guest-houses/1/details"
curl -i "http://localhost:8080/api/v1/staff-recruitment/1/details"

# 5. 찜 정렬/API가 실제로 열렸는지 확인
curl -i -G "http://localhost:8080/api/v1/guest-houses" --data-urlencode "pageNumber=0" --data-urlencode "sort=찜_많은순"
curl -i -G "http://localhost:8080/api/v1/staff-recruitment" --data-urlencode "pageNumber=0" --data-urlencode "sort=찜_많은순"
curl -i -X POST http://localhost:8080/api/v1/wish/guest-houses/1
curl -i "http://localhost:8080/api/v1/wish/guest-houses/my?pageNumber=0"
curl -i "http://localhost:8080/api/v1/wish/staff-recruitment/my?pageNumber=0"

# 6. Swagger 문서 확인
curl -s http://localhost:8080/v3/api-docs

# 7. 새 이미지/JAR 반영 확인
docker image inspect server:geharbang --format 'Created={{.Created}} Id={{.Id}}'
docker exec server-dev stat /server/dev.jar
```

`OPTIONS` 응답의 `Allow` 헤더에 새 method가 없으면 운영 서버에 아직 반영되지 않은 것이다.
예를 들어 게스트하우스 수정 API가 반영된 상태라면 `Allow`에 `PUT`이 포함된다.
찜 API는 비로그인 요청에서 `401 LOGIN_REQUIRED`가 나오면 엔드포인트와 인증 처리가 정상이고, `404`면 아직 운영 서버에 반영되지 않은 것이다.
게스트하우스 목록/상세 응답의 찜 필드는 `isWished`여야 하며, 대표 이미지가 없는 데이터는 `imageUrl: ""`로 내려간다.
`server-dev` 재시작 로그만으로 반영 완료를 판단하지 말고, API 응답과 image/JAR 시각을 같이 본다.

**배포 실패/미반영 체크리스트:**

- `docker logs server-dev --tail 80`에서 앱이 정상 시작했는지 확인
- `docker image inspect server:geharbang`으로 image 생성 시각 확인
- `docker exec server-dev stat /server/dev.jar`로 컨테이너 내부 JAR 시각/크기 확인
- `/Users/brains/guesthouse/jeju/archive/build/libs/dev.jar`가 최신 artifact인지 확인
- `docker build`가 `docker-credential-desktop` 오류로 실패하면 deploy script의 PATH 설정 확인
- GitHub Actions에서 artifact 복사 단계는 `test -f`로 파일 존재 여부를 먼저 검증해야 한다

---

## 14. 금지 사항 (하지 말 것)

| 금지 | 이유 |
|------|------|
| `@Autowired` 필드 주입 | 테스트 어렵고, 순환 참조 감지 늦음 |
| Entity에 setter | 상태 변경이 어디서든 일어날 수 있어 추적 불가 |
| Controller에 비즈니스 로직 | 테스트 불가, 레이어 오염 |
| `@Enumerated(EnumType.ORDINAL)` | Enum 순서 변경 시 DB 데이터 깨짐 |
| 트랜잭션 없이 여러 DB 저장 | 일부만 저장되는 상황 발생 가능 |
| `main` 브랜치에 직접 push | 배포 파이프라인 오염 |
| Spring Security 도입 | 이 프로젝트는 `UserIdResolver`로 인증 처리, Security 추가 시 충돌 |
| `@Transactional` 없이 Entity 상태 변경 | 변경 감지(dirty checking)가 동작하지 않아 DB에 반영 안 됨 |
| `DTO`에 Entity 직접 노출 | 레이어 간 결합도 높아짐, 순환 참조 위험 |
| 필수 컬렉션에 `@Size`만 사용 | null이 통과해 서비스에서 NPE/500 발생 가능 |
| 동일 이름 DTO를 Swagger 이름 없이 여러 도메인에 추가 | API 문서 schema가 충돌해 잘못된 설명/required가 표시될 수 있음 |
