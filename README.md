# Geharbang-BE

게하르방 서비스의 Spring Boot API 서버다. 게스트하우스·스텝 공고·지원·리뷰·
채팅 기능과 함께 RAG 서버를 연결하는 AI 채팅 게이트웨이를 제공한다.

## 로컬 실행

Java 21과 MySQL을 준비하고 `.env`에 필요한 환경변수를 설정한다.

```env
DB_URL=jdbc:mysql://localhost:3306/geharbang
DB_USERNAME=
DB_PASSWORD=
GEHARBANG_AI_BASE_URL=http://localhost:8001
AI_DATA_KEY=
IMAGE_DIRECTORY_PATH=
CERTIFICATE_DIRECTORY_PATH=
```

OAuth2와 JWT 관련 값은 `src/main/resources/application.yml`의 환경변수 목록을
함께 확인한다. `.env`는 저장소에 커밋하지 않는다.

```bash
./gradlew bootRun
```

전체 테스트:

```bash
./gradlew test
```

## AI 챗봇 연동

- 앱은 Spring의 `/api/v1/ai/*`만 호출하고 Spring이 내부 RAG 서버로 요청을 전달한다.
- 비로그인 채팅은 저장하지 않으며, 로그인 사용자의 메시지와 RAG 문맥은 DB에 저장한다.
- 이미지 질문은 5MB 이하 JPEG, PNG, WebP, HEIC/HEIF만 허용한다. MIME 문자열과 실제 파일 시그니처를 모두 검증한다.
- `AI_DATA_KEY`는 게스트하우스·스텝 공고 인덱스 동기화용이며 RAG 서버와 동일해야 한다.

주요 API:

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/api/v1/ai/chat` | 선택 | 텍스트 질문 전송 |
| POST | `/api/v1/ai/chat/image` | 선택 | 이미지와 질문 전송 |
| GET | `/api/v1/ai/conversations` | 필요 | 내 대화 목록 조회 |
| GET | `/api/v1/ai/conversations/{sessionId}` | 필요 | 대화와 메시지 조회 |
| DELETE | `/api/v1/ai/conversations/{sessionId}` | 필요 | 내 대화 삭제 |

로그인 대화 저장 기능을 운영 DB에 적용하기 전에 다음 SQL을 실행한다.

```text
src/main/resources/db/manual/20260722_create_ai_conversation_history.sql
```

상세 구조와 전체 API는 `ARCHITECTURE.md`를 참고한다.
