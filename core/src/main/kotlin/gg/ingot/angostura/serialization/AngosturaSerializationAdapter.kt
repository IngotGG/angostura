package gg.ingot.angostura.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.serializer
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
    fun deserialize(obj: Any, kClass: KClass<*>, arrayType: KClass<*>?): Any?

    /**
     * Serialize the given object into a string.
     * @param obj The object to serialize.
     * @param kClass The class of the object.
     * @return The serialized object.
     */
    fun serialize(obj: Any, kClass: KClass<*>, arrayType: KClass<*>?): String

    /** Adapter for kotlinx.serialization serialization. */
    class Kotlinx(private val json: kotlinx.serialization.json.Json) : AngosturaSerializationAdapter {
        override fun deserialize(obj: Any, kClass: KClass<*>, arrayType: KClass<*>?): Any {
            val serializer = if(arrayType != null) {
                retrieveListSerializer(arrayType)
            } else {
                retrieveKSerializer(kClass)
            } ?: error("No serializer found for type: ${kClass.simpleName}")

            return json.decodeFromString(serializer, obj.toString())
        }

        override fun serialize(obj: Any, kClass: KClass<*>, arrayType: KClass<*>?): String {
            val serializer = if(arrayType != null) {
                retrieveListSerializer(arrayType)
            } else {
                retrieveKSerializer(kClass)
            } ?: error("No serializer found for type: ${kClass.simpleName}")

            return json.encodeToString(serializer, obj)
        }

        /**
         * Retrieve the KSerializer for the given class.
         * @param kClass The class to retrieve
         * @return The KSerializer for the class.
         */
        private fun retrieveKSerializer(kClass: KClass<*>): KSerializer<Any>? {
            return json.serializersModule.serializerOrNull(kClass.java)
        }

        /**
         * Retrieve the KSerializer for the given class.
         * @param kClass The class to retrieve
         * @return The KSerializer for the class.
         */
        @Suppress("UNCHECKED_CAST")
        private fun retrieveListSerializer(kClass: KClass<*>): KSerializer<Any>? {
            val listSerializer = ListSerializer(json.serializersModule.serializerOrNull(kClass.java) ?: return null)
            return listSerializer as KSerializer<Any>
        }
    }

    /** Adapter for Gson serialization. */
    class Gson(private val gson: com.google.gson.Gson) : AngosturaSerializationAdapter {
        override fun deserialize(obj: Any, kClass: KClass<*>, arrayType: KClass<*>?): Any {
            return gson.fromJson(obj.toString(), kClass.java)
        }

        override fun serialize(obj: Any, kClass: KClass<*>, arrayType: KClass<*>?): String {
            return gson.toJson(obj, kClass.java)
        }
    }
}