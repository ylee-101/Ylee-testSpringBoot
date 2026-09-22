# demo

Kotlin + Spring Boot(WebFlux) 기반 중계 서버. `offline-build` 브랜치에는 네트워크 연결 없이
clone 직후 build/test까지 끝낼 수 있는 오프라인 빌드 구성이 포함되어 있습니다.

## 사전 준비물

- **JDK 21** (Eclipse Temurin 권장) — 반드시 사전 설치되어 있어야 합니다. 이 저장소는 JDK를
  번들하지 않습니다. `build-offline.sh`가 아래 위치를 자동으로 탐색합니다.
  - `JAVA_HOME` 환경변수
  - `~/.gradle/jdks/*21*/*/Contents/Home` (Gradle이 이전에 자동 설치한 toolchain)
  - `/Library/Java/JavaVirtualMachines/*21*/Contents/Home`
  - `/opt/homebrew/opt/openjdk@21/...` (Homebrew)
  - 위 어디에도 없으면 스크립트가 에러 메시지와 함께 즉시 종료됩니다.
- Git (clone 용도)
- macOS/Linux 셸(`bash`, `shasum`, `unzip`) — Windows는 WSL 또는 Git Bash 사용

인터넷 접속, `~/.gradle` 글로벌 캐시, Maven Central/Gradle Plugin Portal 접근은 전혀
필요하지 않습니다.

## 오프라인 빌드 성공까지의 과정

### 1. 저장소 clone

```bash
git clone -b offline-build git@github.com:ylee-101/Ylee-testSpringBoot.git
cd Ylee-testSpringBoot
```

### 2. (선택) 분할된 대용량 파일 재조립만 미리 하고 싶다면

GitHub의 파일 크기 제한(100MB) 때문에 Gradle 배포판 zip은 Git LFS 없이
`offline-tools/gradle-dist/gradle-9.7.1-bin.zip.part-aa`, `...part-ab`로 쪼개어
커밋되어 있습니다. `build-offline.sh`가 필요 시 자동으로 재조립하므로 이 단계는
생략해도 되지만, 별도로 확인하고 싶다면:

```bash
./offline-tools/reassemble.sh
```

각 조각을 이어붙인 뒤 커밋된 `.sha256` 체크섬과 대조하여 무결성을 검증합니다.
이미 올바르게 재조립되어 있으면 건너뜁니다.

### 3. 오프라인 빌드 실행

```bash
./build-offline.sh build
```

이 스크립트가 순서대로 수행하는 일:

1. `offline-tools/reassemble.sh`를 호출해 Gradle 9.7.1 배포판 zip을 재조립·검증합니다.
2. 프로젝트 전용 `GRADLE_USER_HOME`(`.gradle-offline-home/`, git-ignored)의 wrapper
   캐시에 재조립된 배포판을 미리 풀어놓아, `./gradlew`가 `services.gradle.org`로
   다운로드를 시도하지 않게 만듭니다.
3. 위에서 설명한 경로들 중 사용 가능한 JDK 21을 탐색해 `JAVA_HOME`으로 지정합니다.
4. 아래 옵션으로 Gradle을 실행합니다.

   ```bash
   ./gradlew build \
     --offline \
     -PofflineMavenOnly=true \
     --gradle-user-home .gradle-offline-home
   ```

   - `--offline`: 네트워크 접근 자체를 차단
   - `-PofflineMavenOnly=true`: `settings.gradle.kts`/`build.gradle.kts`의 저장소 목록을
     `mavenCentral()`/`gradlePluginPortal()` 대신 저장소에 커밋된 `offline-maven/`
     디렉터리(Maven 좌표 레이아웃)만 바라보도록 전환

`build` 대신 다른 task도 그대로 넘길 수 있습니다.

```bash
./build-offline.sh test        # 테스트만
./build-offline.sh assemble    # 컴파일 + jar만, 테스트 생략
```

### 4. 성공 확인

```
BUILD SUCCESSFUL in Ns
7 actionable tasks: 7 executed
```

가 출력되면 오프라인 빌드 성공입니다. 산출물은 `build/libs/`에 생성됩니다.

## 온라인(일반) 개발 시

인터넷 연결과 로컬 JDK 21이 있는 평소 개발 환경에서는 오프라인 스위치 없이 평소처럼
사용하면 됩니다.

```bash
./gradlew build
./gradlew bootRun
```

애플리케이션 실행 후:

```bash
curl -N -i http://localhost:8080/stream
```

## 디렉터리 구조 참고

| 경로 | 설명 |
| --- | --- |
| `offline-maven/` | 이 프로젝트 빌드에 필요한 모든 Maven 아티팩트(플러그인 마커, parent POM/BOM 포함)를 Maven 좌표 레이아웃으로 저장한 로컬 저장소 |
| `offline-tools/gradle-dist/` | Gradle 9.7.1 배포판 zip을 100MB 미만으로 분할한 조각과 체크섬 |
| `offline-tools/reassemble.sh` | `offline-tools/` 하위의 분할 파일을 원본으로 재조립하는 스크립트 |
| `build-offline.sh` | 재조립 → JDK 21 검증 → 오프라인 Gradle 실행까지 한 번에 처리하는 진입점 |
| `.gradle-offline-home/` | `build-offline.sh`가 사용하는 프로젝트 전용 Gradle 캐시 (git-ignored, 매번 재생성 가능) |

새 의존성을 추가했다면 온라인 상태에서 격리된 `GRADLE_USER_HOME`으로 한 번 빌드해
`offline-maven/`을 갱신해야 오프라인 빌드가 계속 성공합니다.

## 추가 참고

프로젝트 배경, 외부 API 연동 정책, mock 서버 계약 등은
[DEVELOPMENT_ENVIRONMENT_REQUIREMENTS.md](./DEVELOPMENT_ENVIRONMENT_REQUIREMENTS.md)를
참고하세요.
