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
        url = uri("https://maven.pkg.github.com/R2turnTrue/chzzk4j")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("io.github.R2turnTrue:chzzk4j:0.0.12")
}
```

#### Maven

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/R2turnTrue/chzzk4j</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.github.R2turnTrue</groupId>
        <artifactId>chzzk4j</artifactId>
        <version>0.0.12</version>
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

### CI Workflow (ci.yml)
- main/master 브랜치에 push 또는 PR 생성 시 실행
- 빌드 및 테스트 수행
- 테스트 결과와 빌드 아티팩트를 1일 동안 보관

### Publish Workflow (publish.yml)
- Release 생성 시 또는 수동 실행
- 빌드 후 GitHub Packages에 배포
- 빌드 아티팩트를 1일 동안 보관

## 버전 업데이트

`build.gradle.kts` 파일의 `version` 속성을 수정:

```kotlin
version = "0.0.13"
```

## JitPack 지원

이 프로젝트는 JitPack도 지원합니다. `jitpack.yml` 파일이 설정되어 있어 JitPack을 통해서도 패키지를 사용할 수 있습니다.

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.R2turnTrue:chzzk4j:Tag")
}
```
