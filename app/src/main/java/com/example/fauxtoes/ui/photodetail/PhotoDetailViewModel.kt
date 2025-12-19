package com.example.fauxtoes.ui.photodetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fauxtoes.flickr.FlickrApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class PhotoDetailViewModel(
    private val flickrApiService: FlickrApiService,
) : ViewModel() {

    private val _state = MutableStateFlow<PhotoDetailState>(PhotoDetailState.Loading)
    val state: StateFlow<PhotoDetailState> = _state.asStateFlow()

    fun loadPhotoInfo(photoId: String, secret: String) {
        viewModelScope.launch {
            _state.value = PhotoDetailState.Loading

            flickrApiService.getInfo(photoId = photoId, secret = secret)
                .onSuccess { response ->
                    _state.value = PhotoDetailState.Success(response.photo)
                }
                .onFailure { throwable ->
                    _state.value = PhotoDetailState.Error(
                        throwable.message ?: "Failed to load photo details"
                    )
                }
        }
    }
}

