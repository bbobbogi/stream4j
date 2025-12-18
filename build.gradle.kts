plugins {
    id("java")
    `maven-publish`
    `java-library`
    jacoco
}

// Helper function to get property from environment
fun getEnvOrProperty(envKey: String, defaultValue: String = ""): String {
    return System.getenv(envKey) ?: defaultValue
}

group = "io.github.bbobbogi"
version = getEnvOrProperty("VERSION", "0.0.14-SNAPSHOT")

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)

    implementation(libs.jetbrains.annotations)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.java.websocket)
    implementation(libs.selenium)
}

tasks.test {
    useJUnitPlatform()
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "chzzk4j"
            groupId = "io.github.bbobbogi"
            version = project.version.toString()

            from(components["java"])

            pom {
                name = "chzzk4j"
                description = "Unofficial Java API library of CHZZK (치지직, the video streaming service of Naver)"
                url = "https://github.com/bbobbogi/chzzk4j"

                developers {
                    developer {
                        name = "bbobbogi"
                        email = ""
                        url = "https://github.com/bbobbogi"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/bbobbogi/chzzk4j.git"
                    developerConnection = "scm:git:ssh://github.com:bbobbogi/chzzk4j.git"
                    url = "https://github.com/bbobbogi/chzzk4j/tree/master"
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
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/bbobbogi/chzzk4j")
            credentials {
                username = getEnvOrProperty("GITHUB_ACTOR")
                password = getEnvOrProperty("GITHUB_TOKEN")
            }
        }
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