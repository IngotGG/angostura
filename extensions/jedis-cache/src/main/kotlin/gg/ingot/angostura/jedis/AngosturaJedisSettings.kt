package gg.ingot.angostura.jedis

import gg.ingot.angostura.AngosturaExtraSettings
import redis.clients.jedis.JedisCluster
import redis.clients.jedis.JedisPool

/**
 * The settings for the Jedis cache.
 * @param cluster The Jedis cluster to use.
 * @param pool The Jedis pool to use.
 * @param redisKey The key to store the cache under.
 * @since 1.0.0
 */
data class AngosturaJedisSettings(
    val cluster: JedisCluster? = null,
    val pool: JedisPool? = null,
    val redisKey: String = "angostura:cache",
): AngosturaExtraSettings {
    /** Whether the Jedis instance is pooled. */
    val pooled get() = pool != null

    init {
        require(cluster != null || pool != null) { "either cluster or pool must be provided" }
    }
}
