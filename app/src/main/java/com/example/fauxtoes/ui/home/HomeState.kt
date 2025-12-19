package com.example.fauxtoes.ui.home

import com.example.fauxtoes.core.Async
import com.example.fauxtoes.data.PhotoDomainModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class HomeState(
    override val queryInput: String,
    val allPhotos: ImmutableList<PhotoDomainModel>,
    val loadPhotosResult: Async<ImmutableList<PhotoDomainModel>>,
    val lastScrollPosition: Int,
    val currentPage: Int,
) : IHomeScreenContentModel {
    override val feedState: FeedState =
        when (loadPhotosResult) {
            is Async.Success -> FeedState.PhotoGrid(
                photos = allPhotos,
                shouldShowLoadingIndicator = loadPhotosResult.isLoading
            )

            is Async.Fail -> FeedState.Error(loadPhotosResult.error)

            Async.Loading -> if (allPhotos.isEmpty()) {
                FeedState.Loading
            } else {
                FeedState.PhotoGrid(allPhotos, true)
            }

            Async.Uninitialized -> FeedState.Empty
        }


    override val searchEnabled: Boolean = !loadPhotosResult.isLoading

    companion object {
        fun initialState() = HomeState(
            queryInput = "",
            loadPhotosResult = Async.Uninitialized,
            allPhotos = persistentListOf(),
            currentPage = 1,
            lastScrollPosition = 0,
        )
    }
}

sealed interface FeedState {

    data object Empty : FeedState

    data class Error(
        val error: Throwable
    ) : FeedState

    data object Loading : FeedState

    data class PhotoGrid(
        val photos: ImmutableList<PhotoDomainModel>,
        val shouldShowLoadingIndicator: Boolean,
    ) : FeedState

}

interface IHomeScreenContentModel {
    val queryInput: String
    val feedState: FeedState
    val searchEnabled: Boolean
}