package gg.ingot.angostura.cache

import kotlin.time.Duration

/**
 * A cache layer that stores values under a key.
 * For example in Redis if a key set to used it will be represented as
 * `angostura:cache:account:<PROVIDED>`
 * @param T The type of the value to store.
 * @property key The key to store the value under.
 * @property ttl The time to live of the cached value.
 * @property refreshTTL If the time to live should be refreshed when the data is accessed.
 * @see CacheLayer
 * @since 1.0.0
 */
abstract class KeyedCacheLayer<T : Any>(
    protected val key: String,
    ttl: Duration,
    refreshTTL: Boolean = false
) : CacheLayer<T>(ttl, refreshTTL)