package gg.ingot.angostura.cache

import gg.ingot.angostura.Angostura

/**
 * A burst cache is usually used to handle a large influx of
 * requests.
 *
 * The burst cache is composed of two caches, a minor cache and a major cache.
 * The [minorCache] should be faster than the [majorCache] and cache data
 * for a shorter period of time, this cache will be used first to handle the
 * requests.
 * The [majorCache] is usually slower than the [minorCache] and cache data
 * for a longer period of time, this cache will be used when the [minorCache]
 * does not have the data or the data in it has expired.
 *
 * @param T The type of the data to cache.
 * @property minorCache The minor cache.
 * @property majorCache The major cache.
 * @author DebitCardz
 * @since 1.0.0
 */
class BurstCache<T : Any>(
    private val minorCache: Cache<T>,
    private val majorCache: Cache<T>
) : Cache<T> {
    override suspend fun put(key: String, value: T): T {
        minorCache.put(key, value)
        majorCache.put(key, value)

        return value
    }

    override suspend fun invalidate(key: String): Boolean {
        minorCache.invalidate(key)
        majorCache.invalidate(key)

        return true
    }

    override suspend fun clear() {
        minorCache.clear()
        majorCache.clear()
    }

    override suspend fun contains(key: String): Boolean {
        return minorCache.contains(key) || majorCache.contains(key)
    }

    override suspend fun get(key: String): T? {
        return minorCache.get(key) ?: majorCache.get(key)
    }
}

/**
 * Creates a new burst cache.
 * @param T The type of the data to cache.
 * @param minorCache The minor cache.
 * @param majorCache The major cache.
 * @return A new burst cache.
 */
fun <T : Any> Angostura.burstCache(
    minorCache: Cache<T>,
    majorCache: Cache<T>
): Cache<T> = BurstCache(minorCache, majorCache)
