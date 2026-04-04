import java.util.Properties
import java.io.File

plugins {
    id("java")
    `maven-publish`
    `java-library`
    signing
    jacoco
}

// publish.properties 로드 (로컬 개발용, .gitignore에 포함)
val publishProps = Properties().apply {
    val f = rootProject.file("publish.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

// 환경변수 → publish.properties → 기본값 순으로 조회
fun getEnvOrProperty(key: String, defaultValue: String = ""): String {
    return System.getenv(key)
        ?: publishProps.getProperty(key)
        ?: defaultValue
}

group = "io.github.bbobbogi"
version = getEnvOrProperty("VERSION", "1.0.0-SNAPSHOT")

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)

    implementation(libs.jetbrains.annotations)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.selenium)
}

tasks.test {
    useJUnitPlatform {
        excludeTags("manual")
    }
    testLogging {
        showStandardStreams = true
    }
}

tasks.register<Test>("manualTest") {
    useJUnitPlatform {
        includeTags("manual")
    }
    testLogging {
        showStandardStreams = true
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "stream4j"
            groupId = "io.github.bbobbogi"
            version = project.version.toString()

            from(components["java"])

            pom {
                name = "stream4j"
                description = "Unified Java streaming donation API library for CHZZK, ci.me, Toonation and more"
                url = "https://github.com/bbobbogi/stream4j"

                developers {
                    developer {
                        name = "bbobbogi"
                        email = ""
                        url = "https://github.com/bbobbogi"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/bbobbogi/stream4j.git"
                    developerConnection = "scm:git:ssh://github.com:bbobbogi/stream4j.git"
                    url = "https://github.com/bbobbogi/stream4j/tree/master"
                }

                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/license/mit/"
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "MavenCentral"
            url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
            credentials {
                username = getEnvOrProperty("MAVEN_USERNAME")
                password = getEnvOrProperty("MAVEN_PASSWORD")
            }
        }
    }
}

signing {
    // 환경변수 SIGNING_KEY 또는 파일 경로 SIGNING_KEY_FILE 지원
    val signingKey = getEnvOrProperty("SIGNING_KEY").ifEmpty { null }
        ?: getEnvOrProperty("SIGNING_KEY_FILE").ifEmpty { null }?.let { path ->
            File(path).takeIf { it.exists() }?.readText()
        }
    val signingPassword = getEnvOrProperty("SIGNING_PASSWORD").ifEmpty { null }

    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["maven"])
    }
}

// JaCoCo configuration for code coverage
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}