# Team 6 Backend

해커톤 서비스를 위한 Java 21 / Spring Boot 3.5 기반 REST API 서버입니다.

## 기술 스택

- Java 21, Spring Boot 3.5, Gradle
- Spring Web, Data JPA, Security, Validation, Actuator
- MySQL 8.4, Flyway
- JWT (JJWT), springdoc-openapi

## 로컬 실행

1. `.env.example`을 참고해 환경변수를 설정합니다. 운영 환경에서는 충분히 긴 무작위 `JWT_SECRET`을 반드시 사용하세요.
2. `docker compose up -d`로 MySQL을 실행합니다.
3. `./gradlew bootRun` (Windows: `gradlew.bat bootRun`)을 실행합니다.
4. `http://localhost:8080/swagger-ui.html`에서 API를 확인합니다.

기본 로컬 DB는 `team6 / team6`, 데이터베이스는 `team6`입니다. 상태 확인은 `GET /actuator/health`를 사용합니다.

## 인증 API

- `POST /api/v1/auth/signup`: 회원가입
- `POST /api/v1/auth/login`: Access/Refresh Token 발급
- `POST /api/v1/auth/refresh`: Refresh Token으로 토큰 재발급
- `GET /api/v1/sample/public`: 미인증 사용자 호출 예제
- `GET /api/v1/sample/me`: 인증 사용자 호출 예제
- 그 외 `/api/**`: `Authorization: Bearer <access-token>` 필요

모든 응답은 `success`, `code`, `message`, `data` 형식을 따릅니다. API 버전은 `/api/v1` 경로로 관리합니다.

### 가장 간단한 인증 테스트

```bash
# 1. 미인증 사용자도 성공 (200)
curl http://localhost:8080/api/v1/sample/public

# 2. 토큰 없이 보호 API 호출 시 실패 (401)
curl -i http://localhost:8080/api/v1/sample/me

# 3. 회원가입
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123!","name":"테스트 사용자"}'

# 4. 로그인 후 응답의 data.accessToken 값을 복사
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123!"}'

# 5. Access Token으로 보호 API 호출 (200)
curl http://localhost:8080/api/v1/sample/me \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

자동 통합 테스트는 `gradlew.bat clean test`로 실행합니다.

## Flyway 규칙

적용된 migration 파일은 수정하지 않습니다. 스키마 변경은 `V2__description.sql`처럼 새 버전 파일로 추가합니다. JPA는 `ddl-auto=validate`, `open-in-view=false`로 동작합니다.

## 브랜치와 커밋

- 브랜치: `main`, `develop`, `feature/*`, `fix/*`, `refactor/*`
- 커밋: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`

GitHub Actions는 `main`/`develop` push 및 pull request에서 테스트와 빌드를 수행합니다.

## 선택 확장

Redis, QueryDSL, OAuth2, Testcontainers, S3, 모니터링 및 배포 자동화는 도메인 요구사항 확정 후 추가합니다.
