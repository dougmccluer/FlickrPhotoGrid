package com.example.fauxtoes.core.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

/**
 * Moshi adapter that coerces various JSON values to a Boolean:
 * - NUMBER: 0 -> false, non-zero -> true
 * - STRING: case-insensitive match against "true" -> true, otherwise false
 * - BOOLEAN: use the boolean value
 * - NULL: false
 * - other tokens: throw JsonDataException
 *
 * When serializing, booleans are written as integers: 1 for true, 0 for false (null -> 0).
 */
class FlexibleBooleanJsonAdapter : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type != Boolean::class.javaObjectType && type != Boolean::class.javaPrimitiveType) {
            return null
        }
        return BooleanAdapter()
    }

    private class BooleanAdapter : JsonAdapter<Boolean>() {
        override fun fromJson(reader: JsonReader): Boolean {
            return when (val token = reader.peek()) {
                JsonReader.Token.BOOLEAN -> reader.nextBoolean()
                JsonReader.Token.NUMBER -> {
                    try {
                        val num = reader.nextDouble()
                        num != 0.0
                    } catch (e: Exception) {
                        throw JsonDataException("Malformed numeric value for Boolean at path ${reader.path}: ${e.message}")
                    }
                }
                JsonReader.Token.STRING -> {
                    try {
                        val s = reader.nextString()
                        s.equals("true", ignoreCase = true) || s.equals("1")
                    } catch (e: Exception) {
                        throw JsonDataException("Malformed string value for Boolean at path ${reader.path}: ${e.message}")
                    }
                }
                JsonReader.Token.NULL -> {
                    reader.nextNull<Unit>()
                    false
                }
                else -> {
                    throw JsonDataException("Expected BOOLEAN, NUMBER, STRING, or NULL for Boolean at path ${reader.path} but was $token")
                }
            }
        }

        override fun toJson(writer: JsonWriter, value: Boolean?) {
            // Write 1 for true, 0 for false (null -> 0)
            val intValue = if (value == true) 1 else 0
            writer.value(intValue)
        }
    }
}