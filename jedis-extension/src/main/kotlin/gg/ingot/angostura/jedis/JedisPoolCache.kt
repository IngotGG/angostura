package gg.ingot.angostura.jedis

import gg.ingot.angostura.serialization.AngosturaSerializationAdapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import redis.clients.jedis.JedisPool
import kotlin.reflect.KClass
import kotlin.time.Duration

internal open class JedisPoolCache<T : Any>(
    redisKey: String,
    key: String,
    ttl: Duration,
    refreshTTL: Boolean = false,
    private val jedisPool: JedisPool,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    kClass: KClass<*>
): JedisCache<T>(redisKey, key, ttl, refreshTTL, dispatcher, kClass) {
    override fun jedis(): JedisWrapper = JedisPoolWrapper(jedisPool)
}

internal class JedisPoolJsonCache<T : Any>(
    redisKey: String,
    key: String,
    ttl: Duration,
    refreshTTL: Boolean = false,
    jedisPool: JedisPool,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val jsonKClass: KClass<*>,
    private val serializationAdapter: AngosturaSerializationAdapter
): JedisPoolCache<T>(redisKey, key, ttl, refreshTTL, jedisPool, dispatcher, String::class) {
    override suspend fun put(key: String, value: T): T {
        super.putString(key, serializationAdapter.serialize(value, jsonKClass))
        return value
    }

    override suspend fun get(key: String): T? {
        val value = super.get(key)
            ?: return null

        return serializationAdapter.deserialize(value, jsonKClass) as? T
    }
}