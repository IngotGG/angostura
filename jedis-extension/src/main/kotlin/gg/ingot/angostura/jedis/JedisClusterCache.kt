package gg.ingot.angostura.jedis

import gg.ingot.angostura.serialization.AngosturaSerializationAdapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import redis.clients.jedis.JedisCluster
import kotlin.reflect.KClass
import kotlin.time.Duration

internal open class JedisClusterCache<T : Any>(
    redisKey: String,
    key: String,
    ttl: Duration,
    refreshTTL: Boolean = false,
    version: String?,
    private val jedisCluster: JedisCluster,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    kClass: KClass<*>
): JedisCache<T>(redisKey, key, ttl, refreshTTL, version, dispatcher, kClass) {
    override fun jedis() = JedisClusterWrapper(jedisCluster)
}

internal class JedisClusterJsonCache<T : Any>(
    redisKey: String,
    key: String,
    ttl: Duration,
    refreshTTL: Boolean = false,
    version: String?,
    jedisCluster: JedisCluster,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val jsonKClass: KClass<*>,
    private val arrayType: KClass<*>? = null,
    private val serializationAdapter: AngosturaSerializationAdapter
): JedisClusterCache<T>(redisKey, key, ttl, refreshTTL, version, jedisCluster, dispatcher, String::class) {
    override suspend fun put(key: String, value: T): T {
        super.putString(key, serializationAdapter.serialize(value, jsonKClass, arrayType))
        return value
    }

    override suspend fun get(key: String): T? {
        val value = super.get(key)
            ?: return null

        return try {
            serializationAdapter.deserialize(value, jsonKClass, arrayType) as? T
        } catch(ex: Exception) {
            logger?.trace("Failed to deserialize {} from the cache, invalidating.", key)
            super.invalidate(key)
            null
        }
    }
}