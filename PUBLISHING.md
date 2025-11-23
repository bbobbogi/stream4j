# Publishing Guide

## GitHub Packages로 패키지 배포하기

이 프로젝트는 GitHub Actions를 통해 자동으로 GitHub Packages에 배포됩니다.

### 자동 배포 (GitHub Actions)

1. **Release 생성 시 자동 배포**
   - GitHub에서 새로운 Release를 생성하면 자동으로 배포됩니다.
   - Release 생성: GitHub Repository → Releases → Create a new release

2. **수동 배포**
   - GitHub Actions 탭에서 "Publish to GitHub Packages" workflow를 수동으로 실행할 수 있습니다.
   - Actions → Publish to GitHub Packages → Run workflow

### 로컬에서 배포하기

```bash
# 환경 변수 설정
export GITHUB_ACTOR=your-github-username
export GITHUB_TOKEN=your-personal-access-token

# 빌드 및 배포
./gradlew publish
```

### GitHub Personal Access Token 생성

1. GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
2. "Generate new token" 클릭
3. 권한 선택:
   - `write:packages` - 패키지 업로드
   - `read:packages` - 패키지 다운로드
4. 생성된 토큰을 안전하게 보관

### 패키지 사용하기

#### Gradle

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/bbobbogi/chzzk4j")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("io.github.bbobbogi:chzzk4j:VERSION")
}
```

#### Maven

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/bbobbogi/chzzk4j</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.github.bbobbogi</groupId>
        <artifactId>chzzk4j</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```

`~/.m2/settings.xml`에 GitHub 자격 증명 추가:

```xml
<servers>
    <server>
        <id>github</id>
        <username>GITHUB_USERNAME</username>
        <password>GITHUB_TOKEN</password>
    </server>
</servers>
```

## CI/CD Workflows

이 프로젝트는 다음과 같은 자동화된 검증 및 배포 파이프라인을 제공합니다:

### CI Workflow (ci.yml)

**실행 시점:**
- main/master 브랜치에 push
- Pull Request 생성 또는 업데이트
- 수동 실행 (workflow_dispatch)

**검증 단계:**
1. **Gradle Wrapper 검증** - 빌드 도구의 무결성 확인
2. **Dependencies 체크** - 모든 의존성 패키지 확인
3. **빌드 수행** - 프로젝트 컴파일 및 빌드
4. **테스트 실행** - 전체 테스트 스위트 실행
5. **코드 커버리지** - JaCoCo를 이용한 테스트 커버리지 측정
6. **빌드 아티팩트 검증** - 생성된 JAR 파일 확인

**아티팩트 보관:**
- 테스트 결과 (1일)
- 커버리지 리포트 (1일)
- 빌드 아티팩트 (1일)

### Publish Workflow (publish.yml)

**실행 시점:**
- GitHub Release 생성
- 수동 실행 (workflow_dispatch)

**배포 전 검증:**
1. **Gradle Wrapper 검증**
2. **Dependencies 체크**
3. **테스트 실행** - 모든 테스트 통과 필수
4. **테스트 결과 검증** - 테스트 결과 디렉토리 확인
5. **빌드 수행**
6. **빌드 아티팩트 검증**
   - Main JAR (chzzk4j-VERSION.jar)
   - Sources JAR (chzzk4j-VERSION-sources.jar)
   - Javadoc JAR (chzzk4j-VERSION-javadoc.jar)

**배포:**
- 모든 검증 통과 후 GitHub Packages에 배포
- 테스트 실패 시 배포 중단

**아티팩트 보관:**
- 테스트 결과 (1일)
- 빌드 아티팩트 (1일)

### CodeQL Security Scan (codeql.yml)

**실행 시점:**
- main/master 브랜치에 push
- Pull Request 생성 또는 업데이트
- 매주 월요일 자정 (정기 스캔)
- 수동 실행 (workflow_dispatch)

**검사 내용:**
- 보안 취약점 자동 탐지
- 코드 품질 문제 검사
- SQL Injection, XSS, Path Traversal 등 일반적인 취약점 검사
- GitHub Security 탭에 결과 자동 등록

## 코드 커버리지

프로젝트는 JaCoCo를 사용하여 테스트 커버리지를 측정합니다.

### 로컬에서 커버리지 확인

```bash
./gradlew test jacocoTestReport
```

리포트 위치: `build/reports/jacoco/test/html/index.html`

## 버전 업데이트

`build.gradle.kts` 파일의 `version` 속성을 수정:

```kotlin
version = "0.0.13"
```

## 자동 의존성 업데이트 (Dependabot)

이 프로젝트는 Dependabot을 사용하여 의존성을 자동으로 관리합니다.

### 설정 내용

**Gradle 의존성:**
- 매주 월요일 오전 9시 (KST)에 체크
- Production/Development 의존성을 그룹으로 관리
- 최대 10개의 PR 동시 생성 가능
- 자동 라벨: `dependencies`, `gradle`

**GitHub Actions:**
- 매주 월요일 오전 9시 (KST)에 체크
- 최대 5개의 PR 동시 생성 가능
- 자동 라벨: `dependencies`, `github-actions`

### Dependabot PR 처리

Dependabot이 생성한 PR은:
1. 자동으로 리뷰어 및 담당자 할당
2. CI workflow가 자동 실행되어 테스트 및 빌드 검증
3. 모든 검증 통과 시 병합 가능

### Dependabot 명령어

PR 코멘트에서 사용 가능한 명령어:
- `@dependabot rebase` - PR 리베이스
- `@dependabot recreate` - PR 재생성
- `@dependabot merge` - PR 병합
- `@dependabot squash and merge` - Squash 후 병합
- `@dependabot cancel merge` - 병합 취소
- `@dependabot reopen` - 닫힌 PR 재오픈
- `@dependabot close` - PR 닫기
- `@dependabot ignore this dependency` - 이 의존성 무시
- `@dependabot ignore this major version` - 이 major 버전 무시
- `@dependabot ignore this minor version` - 이 minor 버전 무시

## JitPack 지원

이 프로젝트는 JitPack도 지원합니다. `jitpack.yml` 파일이 설정되어 있어 JitPack을 통해서도 패키지를 사용할 수 있습니다.

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.bbobbogi:chzzk4j:Tag")
}
```
