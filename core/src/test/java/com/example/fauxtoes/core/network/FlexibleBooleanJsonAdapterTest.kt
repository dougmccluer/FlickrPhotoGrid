package com.example.fauxtoes.core.network

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class FlexibleBooleanJsonAdapterTest {

    private lateinit var moshi: Moshi

    @Before
    fun setUp() {
        moshi = Moshi.Builder()
            .add(FlexibleBooleanJsonAdapter())
            .build()
    }

    @Test
    fun fromJsonHandlesBooleanValues() {
        val adapter = moshi.adapter(Boolean::class.java)
        assertEquals(true, adapter.fromJson("true"))
        assertEquals(false, adapter.fromJson("false"))
    }

    @Test
    fun fromJsonHandlesNumericValues() {
        val adapter = moshi.adapter(Boolean::class.java)
        assertEquals(true, adapter.fromJson("1"))
        assertEquals(false, adapter.fromJson("0"))
    }

    @Test
    fun fromJsonHandlesStringValues() {
        val adapter = moshi.adapter(Boolean::class.java)
        assertEquals(true, adapter.fromJson("\"true\""))
        assertEquals(false, adapter.fromJson("\"false\""))
    }

    @Test
    fun fromJsonHandlesNullValues() {
        val adapter = moshi.adapter(Boolean::class.java)
        assertEquals(false, adapter.fromJson("null"))
    }

    @Test
    fun fromJsonThrowsOnMalformedTokens() {
        val adapter = moshi.adapter(Boolean::class.java)
        assertThrows(JsonDataException::class.java) { adapter.fromJson("{}") }
        assertThrows(JsonDataException::class.java) { adapter.fromJson("[]") }
    }

    @Test
    fun fromJsonHandlesNegativeNumberAsTrue() {
        val adapter = moshi.adapter(Boolean::class.java)
        assertEquals(true, adapter.fromJson("-1"))
        assertEquals(true, adapter.fromJson("-42"))
    }

    @Test
    fun fromJsonHandlesDecimalNumbers() {
        val adapter = moshi.adapter(Boolean::class.java)
        assertEquals(false, adapter.fromJson("0.0"))
        assertEquals(true, adapter.fromJson("0.1"))
        assertEquals(true, adapter.fromJson("1.5"))
    }

    @Test
    fun fromJsonHandlesStringCaseInsensitivity() {
        val adapter = moshi.adapter(Boolean::class.java)
        assertEquals(true, adapter.fromJson("\"TRUE\""))
        assertEquals(true, adapter.fromJson("\"True\""))
        assertEquals(true, adapter.fromJson("\"TrUe\""))
    }

    @Test
    fun fromJsonHandlesStringsAsFalse() {
        val adapter = moshi.adapter(Boolean::class.java)
        assertEquals(false, adapter.fromJson("\"false\""))
        assertEquals(false, adapter.fromJson("\"no\""))
        assertEquals(false, adapter.fromJson("\"yes\""))
        assertEquals(false, adapter.fromJson("\"\""))
    }

    @Test
    fun fromJsonHandlesIntegerString() {
        val adapter = moshi.adapter(Boolean::class.java)
        assertEquals(true, adapter.fromJson("\"1\""))
    }

    @Test
    fun toJsonSerializesBooleansAsIntegers() {
        val adapter = moshi.adapter(Boolean::class.java)
        assertEquals("1", adapter.toJson(true))
        assertEquals("0", adapter.toJson(false))
    }

    @Test
    fun toJsonSerializesNullAsZero() {
        val adapter = moshi.adapter(Boolean::class.java)
        assertEquals("0", adapter.toJson(null))
    }
}

