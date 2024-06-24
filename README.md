<a href="https://ingot.gg/">
    <p align="center">
        <img width="225" height="225" src="https://raw.githubusercontent.com/IngotGG/branding/master/branding.svg" alt="iron"/>
    </p>
</a>

<p align="center">
    <strong>Angostura, a fast caching layer.</strong>
</p>

--- 

# Angostura [![](https://jitpack.io/v/gg.ingot/angostura.svg)](https://jitpack.io/#gg.ingot/angostura) [![](https://jitci.com/gh/IngotGG/angostura/svg)](https://jitci.com/gh/IngotGG/angostura)

Angostura is a quick and easy caching layer used by [ingot.gg](https://ingot.gg) backend services to handle heavy database load and used for general caching.

Feel free to read the [Contribution Guide](https://github.com/IngotGG/angostura/blob/master/CONTRIBUTING.md) to learn how to contribute to Iron or report issues.

## Importing

Tags & Releases can be found on our [Jitpack](https://jitpack.io/#gg.ingot/angostura).

### Gradle

```kts
repositories {
  maven("https://jitpack.io")
}

dependencies {
    implementation("gg.ingot:angostura-core:TAG")
}
```
### Maven
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>gg.ingot</groupId>
    <artifactId>angostura-core</artifactId>
    <version>TAG</version>
</dependency>
```

## Features
* [Memory Caching](#memory-cache)
* [Jedis Support](#jedis)
* [Serialization Support](#serialization)
* [Burst Request Cache](#burst-cache)
* Kotlin Coroutines Support

## Basic Usage

### Memory Cache
```kotlin
suspend fun main() {
    val angostura = Angostura()
    
    val cache = angostura.memoryCache<String>(ttl = 5.seconds)
    val value = cache.getOrCache("key") {
        "value"
    }
    
    println(value)
}
```

### Jedis
Jedis is supported as an extension package of Angostura.
```kotlin
suspend fun main() {
    val jedis = JedisPool("localhost", 6379)

    val angostura = Angostura(
        AngosturaSettings().withExtra(AngosturaJedisSettings( // we pass the extra config
            pool = jedis
        ))
    )
    
    // we can use it in exactly the same way as the memory cache
    val cache = angostura.jedisCache<String>(ttl = 5.seconds, refreshTTL = true)
    val value = cache.getOrCache("key") {
        "value"
    }
    
    println(value)
}
```

### Serialization
Serialization is usually used by out-of-memory caches like Redis.
```kotlin
@Serializable
data class User(val id: Int, val name: String)

suspend fun main() {
    val jedis = JedisPool("localhost", 6379)

    val angostura = Angostura(
        AngosturaSettings(
            // simply pass a serialization adapter
            serializationAdapter = AngosturaSerializationAdapter.Kotlinx(Json)
        ).withExtra(AngosturaJedisSettings( // we pass the extra config
            pool = jedis
        ))
    )

    // we can use it in exactly the same way as the memory cache
    val cache = angostura.jedisCache<User>(ttl = 5.seconds, refreshTTL = true)
    // Will automatically serialize and deserialize the object
    val value = cache.getOrCache(1.toString()) {
        User(1, "Ingot")
    }

    println(value)
}
```

### Burst Requests
```kotlin
@Serializable
data class HotResource(val id: Int, val randomData: String)

suspend fun main() {
    val jedis = JedisPool("localhost", 6379)

    val angostura = Angostura(
        AngosturaSettings(
            // simply pass a serialization adapter
            serializationAdapter = AngosturaSerializationAdapter.Kotlinx(Json)
        ).withExtra(AngosturaJedisSettings( // we pass the extra config
            pool = jedis
        ))
    )

    // Burst Caches are useful for resources that are requested frequently
    // and aren't too important to constantly be up-to-date.
    val cache = angostura.burstCache<HotResource>(
        // first hit the minor cache which should be faster
        minorCache = angostura.memoryCache(ttl = 2.seconds),
        // then fallback to the major cache
        majorCache = angostura.jedisCache(ttl = 3.minutes, refreshTTL = true)
    )
    val value = cache.getOrCache(1.toString()) {
        HotResource(1, "Ingot Team")
    }

    println(value)
}
```