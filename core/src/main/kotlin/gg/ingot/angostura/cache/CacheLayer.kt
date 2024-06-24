package gg.ingot.angostura.cache

import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * Cache layer that holds a time-to-live (TTL) and a refresh TTL flag.
 * @param T The type of the cache.
 * @param ttl The time-to-live for the cache.
 * @param refreshTTL Whether to refresh the TTL when the cache is accessed.
 * @see Cache
 * @since 1.0.0
 */
abstract class CacheLayer<T : Any>(
    val ttl: Duration,
    val refreshTTL: Boolean = false
) : Cache<T> {
    companion object {
        /** The supported primitive types for string transformations. */
        val stringTransformations = mapOf(
            Int::class to { str: String -> str.toIntOrNull() },
            Long::class to { str: String -> str.toLongOrNull() },
            Double::class to { str: String -> str.toDoubleOrNull() },
            Float::class to { str: String -> str.toFloatOrNull() },
            Short::class to { str: String -> str.toShortOrNull() },
            Byte::class to { str: String -> str.toByteOrNull() },
            Boolean::class to { str: String -> str.toBoolean() },
            Char::class to { str: String -> if (str.length == 1) str.first() else error("string is not a char") },
            String::class to { str: String -> str }
        )

        /** The supported primitive types for string transformations. */
        val supportedPrimitiveTypes: Set<KClass<*>>
            get() = stringTransformations.keys

        /**
         * Convert a string to a primitive type.
         * @param str The string to convert.
         * @param kClass The class of the primitive type.
         * @return The converted primitive type.
         */
        fun <T> convertString(str: String, kClass: KClass<*>): T {
            val transformation = stringTransformations[kClass]
                ?: error("no transformation found for $kClass")
            return transformation(str) as T
        }
    }
}
