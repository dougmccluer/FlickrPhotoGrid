package com.example.fauxtoes.flickr

import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrApiService {
    @GET("services/rest/?method=flickr.photos.getRecent")
    suspend fun getRecentPhotos(
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1
    ): Result<FlickrRecentPhotosResponse>

    @GET("services/rest/?method=flickr.photos.search")
    suspend fun searchPhotos(
        @Query("text") text: String,
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1
    ): Result<FlickrSearchPhotosResponse>

    @GET("services/rest/?method=flickr.photos.getInfo")
    suspend fun getInfo(
        @Query("photo_id") photoId: String,
        @Query("secret") secret: String? = null
    ): Result<FlickrPhotoInfoResponse>

}
