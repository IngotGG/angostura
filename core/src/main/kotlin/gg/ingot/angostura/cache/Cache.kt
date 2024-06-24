package gg.ingot.angostura.cache

/**
 * A cache is a data structure that stores data in memory or on disk.
 * The cache is used to store data that is frequently accessed and
 * is expensive to compute.
 * @param V The type of the data to cache.
 * @since 1.0.0
 */
interface Cache <V : Any> {
    /**
     * Put a value into the cache.
     * @param key The key to store the value under.
     * @param value The value to store.
     * @return The value stored.
     */
    suspend fun put(key: String, value: V): V

    /**
     * Invalidate a value in the cache.
     * @param key The key to invalidate.
     * @return If the value was invalidated.
     */
    suspend fun invalidate(key: String): Boolean

    /**
     * Clear the cache.
     * This will remove all values from the cache.
     */
    suspend fun clear()

    /**
     * Check if the cache contains a value.
     * @param key The key to check.
     * @return If the cache contains the value.
     */
    suspend fun contains(key: String): Boolean

    /**
     * Get a value from the cache.
     * @param key The key to get the value from.
     * @return The value from the cache.
     */
    suspend fun get(key: String): V?

    /**
     * Get a value from the cache or return a default value.
     * @param key The key to get the value from.
     * @param or The default value to return if the value is not in the cache.
     * @return The value from the cache or the default value.
     */
    suspend fun getOr(key: String, or: suspend () -> V?): V? {
        return get(key) ?: or()
    }

    /**
     * Get a value from the cache or cache the default value.
     * @param key The key to get the value from.
     * @param or The default value to cache if the value is not in the cache.
     * @return The value from the cache or the default value.
     */
    suspend fun getOrCache(key: String, or: suspend () -> V?): V? {
        return get(key) ?: or()?.also { put(key, it) }
    }
}