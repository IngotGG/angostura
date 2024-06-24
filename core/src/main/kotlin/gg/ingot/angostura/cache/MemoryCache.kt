package gg.ingot.angostura.cache

import gg.ingot.angostura.Angostura
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.time.Duration

/**
 * A cache layer that stores values in memory.
 * @param T The type of the value to store.
 */
class MemoryCache<T : Any>(
    ttl: Duration,
    refreshTTL: Boolean = false
): CacheLayer<T>(ttl, refreshTTL) {
    private val memoryCache = mutableMapOf<String, CachedValue<T>>()

    init {
        // Register the memory cache for garbage collection.
        Angostura.memoryCaches[this] = true
    }

    override suspend fun put(key: String, value: T): T {
        memoryCache[key] = CachedValue(value)
        return value
    }

    override suspend fun invalidate(key: String): Boolean {
        return memoryCache.remove(key) != null
    }

    override suspend fun clear() {
        memoryCache.clear()
    }

    override suspend fun contains(key: String): Boolean {
        return memoryCache.containsKey(key)
    }

    override suspend fun get(key: String): T? {
        val cachedValue = memoryCache[key]
            ?: return null

        if (refreshTTL) {
            cachedValue.refresh()
        }

        return if (cachedValue.isExpired(ttl)) {
            memoryCache.remove(key)
            null
        } else {
            cachedValue.value
        }
    }

    /**
     * Garbage collect the memory cache.
     * This will remove any expired entries from the memory cache.
     * @see CachedValue
     */
    internal fun garbageCollect() {
        memoryCache.entries.removeIf { it.value.isExpired(ttl) }
    }

    /**
     * A value cached in memory.
     * @property value The value to cache.
     */
    private data class CachedValue<T>(val value: T) {
        /** The timestamp of last cache. */
        private var timestamp = Instant.now()

        /**
         * Refresh the timestamp.
         */
        fun refresh() {
            timestamp = Instant.now()
        }

        /**
         * Check if the cached value is expired.
         * @param ttl The time to live of the cached value.
         * @return If the cached value is expired.
         */
        fun isExpired(ttl: Duration): Boolean {
            return Instant.now().isAfter(
                timestamp.plus(ttl.inWholeMilliseconds, ChronoUnit.MILLIS)
            )
        }
    }
}

/**
 * Create a new memory cache.
 * @param T The type of the data to cache.
 * @param ttl The time to live of the cached data.
 * @param refreshTTL If the time to live should be refreshed when the data is accessed.
 * @return The new memory cache.
 */
fun <T : Any> Angostura.memoryCache(
    ttl: Duration = settings.defaultTTL ?: error("ttl is required"),
    refreshTTL: Boolean? = settings.defaultRefreshTTL
): Cache<T> = MemoryCache(ttl, refreshTTL ?: false)