package gg.ingot.angostura.serialization

import kotlinx.serialization.Serializable
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.KClass

/**
 * Adapter for serialization between Kotlin Objects and Strings.
 * @author DebitCardz
 * @since 1.0
 */
interface AngosturaSerializationAdapter {
    /**
     * Deserialize the given object into the given class.
     * @param obj The object to deserialize.
     * @param kClass The class to deserialize the object into.
     * @return The deserialized object.
     */
    fun deserialize(obj: Any, kClass: KClass<*>): Any?

    /**
     * Serialize the given object into a string.
     * @param obj The object to serialize.
     * @param kClass The class of the object.
     * @return The serialized object.
     */
    fun serialize(obj: Any, kClass: KClass<*>): String

    /** Adapter for kotlinx.serialization serialization. */
    class Kotlinx(private val json: kotlinx.serialization.json.Json) : AngosturaSerializationAdapter {
        override fun deserialize(obj: Any, kClass: KClass<*>): Any {
            val serializer = json.serializersModule.serializerOrNull(kClass.java)
                ?: error("No serializer found for type: ${kClass.simpleName}")
            return json.decodeFromString(serializer, obj.toString())
        }

        override fun serialize(obj: Any, kClass: KClass<*>): String {
            val clazz = kClass.java

            // For polymorphic serialization we need to use the base class that'll
            // include the "type" field in the JSON output, so we can then properly
            // deserialize the value, so we literally have to check if the super class
            // is serializable and use that instead of the actual class. - tech
            val serializerClazz = clazz.superclass.takeIf { it.annotations.any { a -> a is Serializable } }
                ?: clazz
            val serializer = json.serializersModule.serializerOrNull(serializerClazz)
                ?: error("No serializer found for type: ${clazz.simpleName}")

            return json.encodeToString(serializer, obj)
        }
    }

    /** Adapter for Gson serialization. */
    class Gson(private val gson: com.google.gson.Gson) : AngosturaSerializationAdapter {
        override fun deserialize(obj: Any, kClass: KClass<*>): Any {
            return gson.fromJson(obj.toString(), kClass.java)
        }

        override fun serialize(obj: Any, kClass: KClass<*>): String {
            return gson.toJson(obj, kClass.java)
        }
    }
}