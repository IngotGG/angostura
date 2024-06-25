plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

allprojects {
    version = "1.0.2"
    group = "gg.ingot"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    kotlin {
        jvmToolchain(17)
    }

    publishing {
        publications {
            register<MavenPublication>("maven") {
                groupId = "${project.group}"
                artifactId = "angostura-${project.name}"
                version = "${project.version}"

                from(components["java"])
            }
        }
    }
}
