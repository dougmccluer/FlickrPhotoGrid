package com.example.fauxtoes.di

import com.example.fauxtoes.BuildConfig
import com.example.fauxtoes.core.network.FlexibleBooleanJsonAdapter
import com.example.fauxtoes.flickr.FlickrApiService
import com.example.fauxtoes.flickr.FlickrInterceptor
import com.example.fauxtoes.core.network.ResultCallAdapterFactory
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


@Module
@Configuration
@ComponentScan("com.example.fauxtoes")
class AppModule {
    @Single
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(FlexibleBooleanJsonAdapter())
        .build()

    @Single
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(FlickrInterceptor(BuildConfig.FLICKR_API_KEY))
            .addInterceptor( HttpLoggingInterceptor().apply { HttpLoggingInterceptor.Level.BODY })
            .build()

    @Single
    fun provideRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .client(okHttpClient)
            .build()

    @Single
    fun provideFlickrApiService(retrofit: Retrofit): FlickrApiService =
        retrofit.create(FlickrApiService::class.java)
}
