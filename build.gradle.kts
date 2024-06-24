plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

group = "gg.ingot"
version = "1.0.0"

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
        mavenLocal()
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    kotlin {
        jvmToolchain(17)
    }
}

subprojects {
    publishing {
        publications {
            register<MavenPublication>("gpr") {
                from(components["java"])

                artifactId = "angostura-${project.name}"
            }
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":extensions:jedis-cache"))
}
