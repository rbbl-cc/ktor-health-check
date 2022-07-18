plugins {
    kotlin("jvm") version "1.7.0"
    id("maven-publish")
    signing
    idea
}

group = "cc.rbbl"
version = System.getenv("BUILD_VERSION") ?: "0.0.1-SNAPSHOT"
description = "Ktor feature for health and readiness checks in the Kubernetes style"

defaultTasks = mutableListOf("build")

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.0.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.getByName<Wrapper>("wrapper") {
    description = "Generates gradlew[.bat] scripts for faster execution"
    gradleVersion = "7.4.2"
}

publishing {
    publications {
        create<MavenPublication>("lib") {
            artifactId = "ktor-health-check"
            from(components["java"])
            pom {
                name.set("Ktor Health Check")
                description.set("A small Library to add Kubernetes-Style Health- and Readiness-Checks to Ktor Projects.")
                url.set("https://gitlab.com/rbbl/ktor-health-check")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://gitlab.com/rbbl/ktor-health-check/-/blob/master/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("rbbl-dev")
                        name.set("rbbl-dev")
                        email.set("dev@rbbl.cc")
                    }
                }
                scm {
                    url.set("https://gitlab.com/rbbl/ktor-health-check")
                }
            }
        }
    }
    repositories {
        maven {
            name = "Snapshot"
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            credentials(PasswordCredentials::class)
        }
        maven {
            name = "Central"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials(PasswordCredentials::class)
        }
        mavenLocal()
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    setRequired({
        gradle.taskGraph.allTasks.filter { it.name.endsWith("ToCentralRepository") }.isNotEmpty()
    })
    sign(publishing.publications)
}