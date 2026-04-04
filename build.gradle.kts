import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `java-library`
    id("com.vanniktech.maven.publish") version "0.30.0"
    jacoco
}

group = "io.github.bbobbogi"
version = System.getenv("VERSION") ?: "1.0.0-SNAPSHOT"

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

tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).apply {
        charSet = "UTF-8"
        memberLevel = JavadocMemberLevel.PUBLIC
        addBooleanOption("html5", true)
        addBooleanOption("Xdoclint:all,-missing", true)
        tags("apiNote:a:API Note:")
    }
    isFailOnError = true
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("io.github.bbobbogi", "stream4j", version.toString())

    pom {
        name.set("stream4j")
        description.set("Unified Java streaming donation API library for CHZZK, ci.me, Toonation and more")
        url.set("https://github.com/bbobbogi/stream4j")

        developers {
            developer {
                name.set("bbobbogi")
                url.set("https://github.com/bbobbogi")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/bbobbogi/stream4j.git")
            developerConnection.set("scm:git:ssh://github.com:bbobbogi/stream4j.git")
            url.set("https://github.com/bbobbogi/stream4j/tree/master")
        }

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/license/mit/")
            }
        }
    }
}

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
