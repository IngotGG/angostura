package gg.ingot.angostura

import gg.ingot.angostura.serialization.AngosturaSerializationAdapter
import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * Settings for the Angostura cache.
 * @param defaultTTL The default time-to-live for cache entries.
 * @param defaultRefreshTTL If the time-to-live should be refreshed when the data is accessed.
 * @param serializationAdapter The serialization adapter to use for the cache.
 * @param version The version of the cache.
 * @since 1.0.0
 */
data class AngosturaSettings(
    val defaultTTL: Duration? = null,
    val defaultRefreshTTL: Boolean = false,
    val serializationAdapter: AngosturaSerializationAdapter? = null,
    var version: String? = null
) {
    private val _extraSettings = mutableMapOf<KClass<*>, AngosturaExtraSettings>()
    val extraSettings: Map<KClass<*>, AngosturaExtraSettings>
        get() = _extraSettings

    fun withExtra(settings: AngosturaExtraSettings): AngosturaSettings {
        _extraSettings[settings::class] = settings
        return this
    }
}

/**
 * Extra settings that can be passed into Angostura, usually from
 * extension packages.
 */
interface AngosturaExtraSettings
