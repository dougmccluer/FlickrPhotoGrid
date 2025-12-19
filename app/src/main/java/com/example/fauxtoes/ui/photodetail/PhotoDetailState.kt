package com.example.fauxtoes.ui.photodetail

import com.example.fauxtoes.flickr.FlickrPhotoInfo

sealed interface PhotoDetailState {
    data object Loading : PhotoDetailState
    data class Success(val photoInfo: FlickrPhotoInfo) : PhotoDetailState
    data class Error(val message: String) : PhotoDetailState
}
