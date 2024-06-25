dependencies {
    compileOnly("redis.clients:jedis:5.0.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")

    compileOnly(project(":core"))
}