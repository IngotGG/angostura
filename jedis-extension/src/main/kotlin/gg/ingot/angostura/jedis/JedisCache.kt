package gg.ingot.angostura.jedis

import gg.ingot.angostura.Angostura
import gg.ingot.angostura.AngosturaSettings
import gg.ingot.angostura.cache.Cache
import gg.ingot.angostura.cache.CacheLayer
import gg.ingot.angostura.cache.KeyedCacheLayer
import kotlinx.coroutines.*
import redis.clients.jedis.JedisCluster
import redis.clients.jedis.JedisPool
import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * A cache layer that stores values under a key in a Jedis instance.
 * @param T The type of the value to store.
 * @property redisKey The key to store the cache under.
 * @property kClass The class of the cache.
 */
internal abstract class JedisCache<T : Any>(
    private val redisKey: String,
    key: String,
    ttl: Duration,
    refreshTTL: Boolean = false,
    version: String?,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val kClass: KClass<*>
) : KeyedCacheLayer<T>("${redisKey}:${key}", ttl, refreshTTL, version) {
    private val coroutineScope = CoroutineScope(dispatcher + SupervisorJob())

    /**
     * Put a string into the Jedis cache.
     * @param key The key for the cache.
     * @param value The value to put into the cache.
     */
    protected fun putString(key: String, value: String) {
        coroutineScope.launch {
            jedis().setex(
                buildKey(key),
                ttl.inWholeSeconds,
                value
            )
        }
    }

    override suspend fun put(key: String, value: T): T {
        putString(key, value.toString())
        return value
    }

    override suspend fun invalidate(key: String): Boolean {
        return jedis().del(buildKey(key)) == 1L
    }

    override suspend fun clear() {
        jedis().del(buildKey("*"))
    }

    override suspend fun contains(key: String): Boolean {
        return jedis().exists(buildKey(key))
    }

    override suspend fun get(key: String): T? {
        val value = jedis().get(buildKey(key))
            ?: return null

        if(refreshTTL) {
            jedis().expire(buildKey(key), ttl.inWholeSeconds)
        }

        return convertString(value, kClass)
    }

    /**
     * Get the jedis instance.
     * @return The jedis instance.
     */
    protected abstract fun jedis(): JedisWrapper

    /**
     * Jedis Wrapper to allow for either [JedisPool] or [JedisCluster].
     */
    protected interface JedisWrapper {
        fun setex(key: String, ttl: Long, value: String): String
        fun expire(key: String, ttl: Long): Long
        fun del(key: String): Long
        fun exists(key: String): Boolean
        fun get(key: String): String?
    }

    protected class JedisPoolWrapper(private val pool: JedisPool) : JedisWrapper {
        override fun setex(key: String, ttl: Long, value: String): String = pool.resource.use { it.setex(key, ttl, value) }
        override fun expire(key: String, ttl: Long): Long = pool.resource.use { it.expire(key, ttl) }
        override fun del(key: String): Long = pool.resource.use { it.del(key) }
        override fun exists(key: String): Boolean = pool.resource.use { it.exists(key) }
        override fun get(key: String): String? = pool.resource.use { it.get(key) }
    }

    protected class JedisClusterWrapper(private val cluster: JedisCluster) : JedisWrapper {
        override fun setex(key: String, ttl: Long, value: String): String = cluster.use { it.setex(key, ttl, value) }
        override fun expire(key: String, ttl: Long): Long = cluster.use { it.expire(key, ttl) }
        override fun del(key: String): Long = cluster.use { it.del(key) }
        override fun exists(key: String): Boolean = cluster.use { it.exists(key) }
        override fun get(key: String): String? = cluster.use { it.get(key) }
    }
}

/**
 * Create a new jedis cache.
 * @param settings The angostura settings.
 * @param jedisSettings The jedis settings.
 * @param key The key for the cache.
 * @param ttl The time to live of the cache.
 * @param refreshTTL If the time to live should be refreshed on access.
 * @param dispatcher The coroutine dispatcher.
 * @param kClass The class of the cache.
 * @param arrayType The class of the array type.
 * @return The cache.
 */
fun <T : Any> createJedisCache(
    settings: AngosturaSettings,
    jedisSettings: AngosturaJedisSettings,
    key: String,
    ttl: Duration,
    refreshTTL: Boolean,
    version: String?,
    dispatcher: CoroutineDispatcher,
    kClass: KClass<*>,
    arrayType: KClass<*>? = null
): Cache<T> {
    require(jedisSettings.redisKey.isNotBlank()) { "no redis key set in angostura settings." }

    return if(kClass in CacheLayer.supportedPrimitiveTypes) {
        // primitive cache
        if(jedisSettings.pooled) {
            JedisPoolCache(jedisSettings.redisKey, key, ttl, refreshTTL, version, jedisSettings.pool!!, dispatcher, kClass)
        } else {
            JedisClusterCache(jedisSettings.redisKey, key, ttl, refreshTTL, version, jedisSettings.cluster!!, dispatcher, kClass)
        }
    } else {
        // json cache
        checkNotNull(settings.serializationAdapter) { "no serialization adapter set in angostura settings." }

        if(jedisSettings.pooled) {
            JedisPoolJsonCache(jedisSettings.redisKey, key, ttl, refreshTTL, version, jedisSettings.pool!!, dispatcher, kClass, arrayType, settings.serializationAdapter!!)
        } else {
            JedisClusterJsonCache(jedisSettings.redisKey, key, ttl, refreshTTL, version, jedisSettings.cluster!!, dispatcher, kClass, arrayType, settings.serializationAdapter!!)
        }
    }
}

/**
 * Create a new jedis cache.
 * @param key The key for the cache.
 * @param ttl The time to live of the cache.
 * @param refreshTTL If the time to live should be refreshed on access.
 * @param dispatcher The coroutine dispatcher.
 * @return The cache.
 * @throws IllegalStateException If [AngosturaJedisSettings] are not set.
 */
inline fun <reified T : Any> Angostura.jedisCache(
    key: String? = T::class.java.simpleName,
    ttl: Duration? = settings.defaultTTL ?: error("no ttl specified and no default was set"),
    refreshTTL: Boolean = settings.defaultRefreshTTL,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Cache<T> {
    checkNotNull(key)
    checkNotNull(ttl)

    if(Collection::class.java.isAssignableFrom(T::class.java)) {
        error("use jedisArrayCache for collections.")
    }

    return createJedisCache(
        settings,
        settings.extraSettings[AngosturaJedisSettings::class] as? AngosturaJedisSettings
            ?: error("no angostura jedis settings found."),
        key,
        ttl,
        refreshTTL,
        settings.version,
        dispatcher,
        T::class
    )
}

/**
 * Create a new jedis cache for collections.
 * @param key The key for the cache.
 * @param ttl The time to live of the cache.
 * @param refreshTTL If the time to live should be refreshed on access.
 * @param dispatcher The coroutine dispatcher.
 * @return The cache.
 * @throws IllegalStateException If [AngosturaJedisSettings] are not set.
 */
inline fun <reified T : Any> Angostura.jedisArrayCache(
    key: String? = T::class.java.simpleName,
    ttl: Duration? = settings.defaultTTL ?: error("no ttl specified and no default was set"),
    refreshTTL: Boolean = settings.defaultRefreshTTL,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Cache<List<T>> {
    checkNotNull(key)
    checkNotNull(ttl)

    return createJedisCache(
        settings,
        settings.extraSettings[AngosturaJedisSettings::class] as? AngosturaJedisSettings
            ?: error("no angostura jedis settings found."),
        key,
        ttl,
        refreshTTL,
        settings.version,
        dispatcher,
        List::class,
        T::class
    )
}