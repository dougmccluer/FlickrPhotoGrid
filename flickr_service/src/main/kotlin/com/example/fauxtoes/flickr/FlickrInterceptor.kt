package com.example.fauxtoes.flickr

import okhttp3.Interceptor
import okhttp3.Response

class FlickrInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val originalUrl = original.url
        val newUrl = originalUrl.newBuilder()
            .addQueryParameter("format", "json")
            .addQueryParameter("nojsoncallback", "1")
            .addQueryParameter("api_key", apiKey)
            .build()
        val newRequest = original.newBuilder().url(newUrl).build()
        return chain.proceed(newRequest)
    }
}

