# 개발 환경 및 사전 확인 요건

이 문서는 `demo` 프로젝트를 새 Mac mini에서 처음부터 실행·개발·테스트하기 위해 필요한 환경과 확인 사항을 정리한 것입니다.

## 프로젝트 기준

| 항목 | 현재 값 |
| --- | --- |
| JDK | 21 |
| Gradle | Wrapper가 Gradle 9.7.1 자동 사용 |
| Kotlin | 2.3.21 |
| Spring Boot | 4.1.1 |
| 애플리케이션 포트 | 8080 |
| upstream/mock 서버 포트 | 9090 |
| upstream 호출 경로 | `/mock/stream?scenario=...` |

## 현재 구현 및 검증 완료 사항 (2026-09-15)

외부 API 연결 정보는 코드에 하드코딩하지 않고 아래 Spring 설정으로 관리한다.

```properties
external-api.base-url=http://localhost:9090
external-api.stream-path=/mock/stream
external-api.scenario=accepted-queue
external-api.response-timeout=15s
```

- `ExternalApiProperties`가 위 설정을 타입 안전하게 읽고, `TestClient`와 `TestService`에 주입된다.
- upstream의 HTTP 상태 코드는 유지한다. 따라서 upstream이 `202 Accepted`와 SSE body를 보내면 8080도 `202`와 해당 SSE body를 전달한다.
- SSE 이벤트 사이에 `external-api.response-timeout`만큼 공백이 생겨 timeout되면, 연결 오류로 끊지 않고 아래 마지막 SSE 이벤트를 보낸 뒤 정상 종료한다.

  ```text
  event: done
  data: {"error":"응답 시간이 초과되었습니다"}
  ```

- 위 timeout은 현재 Flux에 들어오는 **모든** SSE 이벤트를 기준으로 한다. heartbeat도 SSE 이벤트로 수신되면 timeout 시간이 다시 시작되므로, 추후에는 실제 업무 응답 이벤트만 기준으로 제한할지 API 계약에서 결정한다.
- 현재 mock 서버를 대상으로 `bootRun`과 `curl -N -i http://localhost:8080/stream` 검증을 완료했다.

## 담당자 설치 및 권한 요청

### 필수 개발 도구

- Eclipse Temurin(OpenJDK) 21 LTS, macOS ARM64 버전
  - 터미널에서 `java -version`으로 JDK 21 확인 가능해야 함
  - `JAVA_HOME` 및 `PATH` 설정 필요
- IntelliJ IDEA
  - Spring Boot 기능이 필요하면 Ultimate 권장
  - 코드 편집과 Gradle 실행 위주라면 Community도 가능
- Git
  - 터미널에서 Git 사용 가능해야 함
  - 사내 Git 저장소 접근 권한 필요
  - Git 사용자 이름/이메일, SSH 키 또는 사내 SSO 인증 설정 필요

### 네트워크, 프록시, 인증서

Gradle Wrapper와 의존성 다운로드를 위해 아래 HTTPS 도메인 접근이 필요합니다.

- `services.gradle.org`
- `plugins.gradle.org`
- `repo.maven.apache.org` 또는 Maven Central
- 사내 Git 저장소 도메인

사내 프록시 또는 사내 CA 인증서를 사용한다면, Gradle과 IntelliJ가 의존성을 내려받을 수 있도록 프록시·인증서 설정도 필요합니다.

### 파일 및 포트 권한

- 프로젝트 디렉터리 읽기/쓰기 및 실행 권한
- 사용자 Gradle 캐시(`~/.gradle`) 읽기/쓰기 권한
- 로컬 8080 포트 사용 가능
- 로컬 9090 포트 사용 가능

## 라이브러리 설치 여부

프로젝트의 라이브러리는 Gradle이 자동으로 내려받으므로 별도 설치가 필요하지 않습니다.

| 라이브러리/도구 | 별도 설치 필요 여부 |
| --- | --- |
| Spring Boot WebFlux | 불필요 |
| Reactor | WebFlux 의존성으로 자동 포함 |
| Netty 서버 | WebFlux 의존성으로 자동 포함 |
| Kotlin reflect | Gradle 자동 다운로드 |
| Jackson Kotlin module | Gradle 자동 다운로드 |
| JUnit 5 및 Spring 테스트 | Gradle 자동 다운로드 |
| Gradle | 불필요 — `./gradlew` 사용 |
| Docker | 현재 프로젝트 기준 불필요 |
| Node.js | 현재 프로젝트 기준 불필요 |

선택적으로 Homebrew와 `ripgrep`(`rg`)를 설치하면 터미널 기반 개발 작업에 편리합니다.

## Git 저장소 확인

현재 checkout에는 Git remote가 설정되어 있지 않을 수 있습니다. 새 장비에서 시작하기 전에 아래 중 하나를 확인해야 합니다.

- clone할 사내 Git 저장소 URL
- 저장소 접근 권한과 인증 방식
- Git 저장소가 없다면 프로젝트 소스를 전달받을 방법

## 9090 mock/upstream 서버

현재 프로젝트는 다음 로컬 upstream API를 호출하도록 작성되어 있습니다.

```text
GET http://localhost:9090/mock/stream?scenario=...
```

외부 API는 아직 개발 중이므로, 현재는 방화벽 요청이 필요하지 않습니다. 외부 API가 준비되면 대상 도메인, HTTPS 443 포트, DNS 해석 필요 여부, 프록시 경유 여부, 사내 CA 인증서 필요 여부를 한 번에 확인해 방화벽 요청합니다.

### 더미파일 확정 시 확인할 계약

더미파일만으로는 현재 프로젝트를 실행할 수 없습니다. 더미파일을 9090 HTTP 응답으로 제공하는 mock 서버 또는 실행 방법이 필요합니다.

더미파일 또는 mock 서버가 확정되면 아래 항목을 확인합니다.

- 파일 형식: JSON, SSE 원문, NDJSON 등
- mock 서버 제공 방식: 별도 소스, 실행 파일, Docker 이미지, mock 도구 설정 등
- mock 서버 실행 포트: 9090 사용 여부
- 응답 HTTP 상태 코드: `200`, `202`, `4xx`, `5xx`
- 응답 `Content-Type`: `text/event-stream`, `application/json` 등
- SSE 이벤트 구조: `event`, `id`, `retry`, `data` 사용 여부
- JSON body 구조와 nullable 필드
- 이벤트 순서와 스트림 종료 조건
- 첫 이벤트 지연 시간과 이벤트 간 지연 시간
- timeout, 연결 종료, 오류 상태 시나리오
- 더미파일/API 계약의 버전 관리 위치와 변경 공지 방식

### 최소 테스트 시나리오

| 시나리오 | 확인할 결과 |
| --- | --- |
| JSON 정상 응답 | JSON 처리 정책에 맞는 응답 |
| SSE 정상 응답 | 이벤트 순서와 데이터 전달 |
| `202 Accepted` + `queue_status` SSE | 202 상태와 SSE body 전달 |
| 첫 이벤트 지연 | timeout 처리 |
| 이벤트 전달 후 지연 또는 오류 | 스트림 오류 처리 |

## 중계 서버 구현 전 확인 사항

이 프로젝트의 역할은 외부 API에서 받은 데이터를 프론트엔드에 전달하는 중계 서버입니다. 외부 API와 프론트엔드의 계약이 확정되기 전에는 아래 항목을 확인해야 구현 범위를 결정할 수 있습니다.

### 1. 외부 데이터 API 연결을 위해 확인할 사항

#### 요청 계약

- 외부 API의 base URL과 호출 경로
- HTTP method: `GET`, `POST` 등
- query parameter, path variable, request body 구조
- 요청 header: `Accept`, `Content-Type`, 추적 ID 등
- 인증 방식: Bearer token, API key, mTLS, 사내 인증 헤더 등
- 인증 정보의 전달 및 보관 방법: 환경변수, Secret 관리 도구 등
- 요청마다 프론트의 사용자 정보 또는 토큰을 전달해야 하는지

#### 응답 계약

- 성공 HTTP status 목록과 의미: 특히 `200`, `202`의 처리 기준
- 오류 HTTP status 목록과 프론트에 전달할 정책
- 응답 `Content-Type`: `text/event-stream`, `application/json`, 또는 둘 다 가능한지
- SSE event 구조: `event`, `id`, `retry`, `data`, comment 사용 여부
- SSE data의 실제 형식: 단순 문자열, JSON 문자열, DTO 구조 등
- JSON body 구조와 nullable 필드
- upstream의 HTTP status와 header를 8080에서 그대로 전달할 범위

#### 스트림 및 오류 정책

- 첫 응답 또는 첫 SSE 이벤트의 허용 대기 시간
- SSE 이벤트 간 허용 공백 시간과 heartbeat 규칙
- 외부 API가 스트림을 정상 종료할 수 있는지, 종료 의미는 무엇인지
- 첫 이벤트 전 timeout/오류 시 HTTP 오류 status와 body 형식
- 이벤트 전송 후 timeout/오류 시 처리 방식
  - 연결 종료
  - `event: error` SSE 전송 후 종료
  - 현재 구현처럼 `event: done`의 data에 오류 JSON을 담아 정상 종료
- 외부 API의 4xx/5xx를 그대로 전달할지, 8080의 공통 오류로 변환할지
- 재시도 허용 여부, 재시도 횟수와 backoff 정책
  - SSE는 중복 이벤트 가능성이 있으므로 이벤트 ID/재개 방식 없이 자동 재시도하지 않음
- 프론트 연결 취소 시 upstream 요청도 취소해야 하는지

#### 네트워크 및 운영 연결

- 개발·스테이징·운영 환경별 외부 API base URL
- 대상 도메인, HTTPS 포트, DNS 해석 필요 여부
- 방화벽 허용, 프록시 경유, 사내 CA 인증서 필요 여부
- 운영 환경의 timeout 및 인증키 설정 방식
- 헬스체크 endpoint와 모니터링/로그 수집 요구사항

### 2. 프론트엔드 및 WebView 연결을 위해 확인할 사항

#### 8080 API 계약

- 프론트가 호출할 8080 endpoint와 HTTP method
- 8080이 항상 SSE로 응답할지, upstream 응답 형식(JSON/SSE)을 그대로 보존할지
- 성공 HTTP status와 의미: 특히 `200`, `202`를 프론트가 구분해야 하는지
- 프론트가 필요한 응답 header와 SSE event 이름
- SSE `data`의 타입: 문자열, JSON 문자열, DTO 구조 등
- timeout 및 오류를 프론트에 알리는 방식
  - HTTP 오류 body
  - `event: error` SSE
  - 연결 종료

#### CORS 및 WebView

- Next.js 페이지의 개발·스테이징·운영 origin
- WebView가 로드하는 실제 페이지 URL
- Next.js rewrite/proxy 사용 여부
  - 프론트 origin과 API origin을 같게 만들면 CORS가 불필요할 수 있음
- API origin이 다를 경우 허용할 CORS origin 목록
- WebView의 API 도메인 allowlist 필요 여부
- Android WebView와 iOS WKWebView의 지원 OS 버전 및 SSE(`EventSource`) 동작 확인
- WebView에서 HTTPS 인증서를 신뢰할 수 있는지

#### 인증 및 추적

- 프론트 인증 방식: 쿠키, Bearer token, 앱 브리지 등
- SSE 연결에서 인증 정보를 전달하는 방법
  - native `EventSource`는 임의의 `Authorization` header를 넣기 어려움
- 쿠키 기반 인증 시 CORS credentials 허용 여부
- credentials 사용 시 허용 origin을 와일드카드(`*`)로 둘 수 없음
- request ID 또는 trace ID를 프론트와 외부 API 사이에 전달할 필요 여부

### 계약 확정 후 권장 구현 순서

1. 외부 API 주소, path, timeout, 인증 정보를 코드에서 분리해 환경별 설정으로 이동
2. 외부 API의 요청·응답 형식과 status 처리 구현
3. timeout, 오류, 스트림 종료 정책 구현
4. mock 서버/더미파일 기반 통합 테스트 작성
5. 프론트 API 계약, CORS, WebView 인증 방식 적용
6. 로그, trace ID, 운영 설정 적용

## 실행 확인

환경이 준비된 뒤 프로젝트 루트에서 아래 명령으로 확인합니다.

```bash
./gradlew test
./gradlew bootRun
```

애플리케이션이 실행되면 다른 터미널에서 다음처럼 SSE 응답을 확인할 수 있습니다.

```bash
curl -N -i http://localhost:8080/stream
```
