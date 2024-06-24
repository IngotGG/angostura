pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "angostura"
include("core")
include("extensions")
include("extensions:jedis-cache")
findProject(":extensions:jedis-cache")?.name = "jedis-cache"
