package gg.ingot.angostura.cache

import kotlin.time.Duration

/**
 * A cache layer that stores values under a key.
 * For example in Redis if a key set to used it will be represented as
 * `angostura:cache:account:<PROVIDED>`
 * @param T The type of the value to store.
 * @property key The key to store the value under.
 * @property version The version of the cache.
 * @property ttl The time to live of the cached value.
 * @property refreshTTL If the time to live should be refreshed when the data is accessed.
 * @see CacheLayer
 * @since 1.0.0
 */
abstract class KeyedCacheLayer<T : Any>(
    private val key: String,
    ttl: Duration,
    refreshTTL: Boolean = false,
    private val version: String?
) : CacheLayer<T>(ttl, refreshTTL) {
    /**
     * Build the key for the cache.
     * @param str The identifier to build the key with.
     * @return The built key.
     */
    protected fun buildKey(str: String): String {
        return if (version != null) {
            "$key:$version:$str"
        } else {
            "$key:$str"
        }
    }
}