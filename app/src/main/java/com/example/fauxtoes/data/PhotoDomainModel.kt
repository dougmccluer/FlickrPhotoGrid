package com.example.fauxtoes.data

import com.example.fauxtoes.flickr.FlickrPhoto
import com.example.fauxtoes.flickr.FlickrPhotoSize
import com.example.fauxtoes.flickr.FlickrPhotoSize.MEDIUM_500

data class PhotoDomainModel(
    val id: String,
    val server: String,
    val secret: String,
    val title:String?,
){
    fun url(size:FlickrPhotoSize = MEDIUM_500):String = "$PHOTO_BASE_URL/$server/${id}_${secret}${size.suffix}.$FILE_EXTENSION"

    companion object{
        const val PHOTO_BASE_URL = "https://live.staticflickr.com"
        const val FILE_EXTENSION = "jpg"
    }
}

fun FlickrPhoto.toPhotoDomainModel() = PhotoDomainModel(
    id = id,
    server = server,
    secret = secret,
    title = title.takeIf { it.isNotBlank() },
)


