package com.example.fauxtoes.data

import com.example.fauxtoes.flickr.FlickrApiService
import com.example.fauxtoes.flickr.FlickrRecentPhotosResponse
import com.example.fauxtoes.flickr.FlickrSearchPhotosResponse
import org.koin.core.annotation.Single

@Single
class PhotosRepository(
    private val flickrApiService: FlickrApiService
) {
    suspend fun searchPhotos(
        query: String,
        page: Int,
        perPage: Int
    ): Result<FlickrSearchPhotosResponse> =
        flickrApiService.searchPhotos(text = query, page = page, perPage = perPage)

    suspend fun getRecentPhotos(
        page: Int,
        perPage: Int,
    ): Result<FlickrRecentPhotosResponse> = flickrApiService.getRecentPhotos(
        perPage = perPage,
        page = page
    )

    suspend fun getPhotoInfo( photoId:String ) = flickrApiService.getInfo(photoId)
}
