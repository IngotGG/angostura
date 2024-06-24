package gg.ingot.angostura

import gg.ingot.angostura.cache.MemoryCache
import kotlinx.coroutines.*
import java.util.Timer
import java.util.WeakHashMap
import kotlin.concurrent.schedule
import kotlin.time.Duration.Companion.seconds

/**
 * Entry point for the caching library.
 * Caches will be constructed against this instance.
 * @param settings The settings for the library.
 * @param dispatcher The dispatcher to use for the library.
 */
class Angostura(
    val settings: AngosturaSettings = AngosturaSettings(),
    val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /** The coroutine scope for the library. */
    private val coroutineScope = CoroutineScope(dispatcher + SupervisorJob())

    init {
        launchMemoryCacheGC()
    }

    /**
     * Launch the memory cache garbage collector.
     * This will remove any expired entries from the memory cache.
     * @see MemoryCache
     */
    private fun launchMemoryCacheGC() {
        Timer().schedule(MEMORY_CACHE_GC_INTERVAL.inWholeMilliseconds) {
            memoryCaches.keys
                .forEach { it.garbageCollect() }
        }
    }

    internal companion object {
        /** The interval to garbage collect memory caches. */
        private val MEMORY_CACHE_GC_INTERVAL = 30.seconds

        /** The memory caches to garbage collect. */
        val memoryCaches = WeakHashMap<MemoryCache<*>, Boolean>()
    }
}