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

group = "com.bbobbogi"
version = getEnvOrProperty("VERSION", "0.2.1-SNAPSHOT")

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
            groupId = "com.bbobbogi"
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
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/bbobbogi/stream4j")
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