plugins {
    id("java")
    `maven-publish`
    `java-library`
    jacoco
}

group = "io.github.R2turnTrue"
version = "0.0.12"

// Helper function to get property from environment
fun getEnvOrProperty(envKey: String, defaultValue: String = ""): String {
    return System.getenv(envKey) ?: defaultValue
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("org.jetbrains:annotations:24.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.java-websocket:Java-WebSocket:1.5.5")
    implementation("org.seleniumhq.selenium:selenium-java:4.26.0")
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
            groupId = "io.github.R2turnTrue"
            version = "0.0.12"

            from(components["java"])

            pom {
                name = "chzzk4j"
                description = "Unofficial Java API library of CHZZK (치지직, the video streaming service of Naver)"
                url = "https://github.com/R2turnTrue/chzzk4j"

                developers {
                    developer {
                        name = "R2turnTrue"
                        email = "r3turntrue@gmail.com"
                        url = "https://github.com/R2turnTrue"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/R2turnTrue/chzzk4j.git"
                    developerConnection = "scm:git:ssh://github.com:R2turnTrue/chzzk4j.git"
                    url = "https://github.com/R2turnTrue/chzzk4j/tree/master"
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
            url = uri("https://maven.pkg.github.com/R2turnTrue/chzzk4j")
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