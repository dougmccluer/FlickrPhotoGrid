package com.example.fauxtoes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fauxtoes.core.Async
import com.example.fauxtoes.data.PhotoDomainModel
import com.example.fauxtoes.data.PhotosRepository
import com.example.fauxtoes.core.toAsync
import com.example.fauxtoes.data.toPhotoDomainModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import timber.log.Timber
import kotlin.mapCatching

@KoinViewModel
class HomeViewModel(
    private val photosRepository: PhotosRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState.initialState())
    val state = _state.asStateFlow()

    private fun setState(update: HomeState.() -> HomeState) {
        _state.value = state.value.update()
    }

    @OptIn(FlowPreview::class)
    private val debounceChannel = Channel<suspend () -> Unit>().apply {
        consumeAsFlow()
            .debounce(PAGE_REFRESH_DELAY_MILLIS)
            .conflate()
            .apply {
                viewModelScope.launch {
                    collect { block -> block() }
                }
            }
    }


    private fun fetchNextPage() = viewModelScope.launch {
        if (state.value.loadPhotosResult.isLoading.not()) {
            setState { copy(currentPage = state.value.currentPage + 1) }
            Timber.d("fetching page ${state.value.currentPage}")
            state.value.queryInput
                .takeIf { it.isNotEmpty() }
                ?.let { query ->
                    setState { copy(queryInput = query) }
                    searchPhotos(searchTerm = query, page = state.value.currentPage)
                } ?: getRecentPhotos(page = state.value.currentPage)
        } else {
            Timber.d("waiting for current page to load")
        }
    }

    private fun startLoading(page: Int) = setState {
        copy(
            loadPhotosResult = Async.Loading,
            allPhotos = if (page <= 1) { persistentListOf() } else { allPhotos }
        )
    }

    private suspend fun searchPhotos(searchTerm: String, page: Int, perPage: Int = PAGE_SIZE) {
        startLoading(page)
        photosRepository.searchPhotos(searchTerm, page = page, perPage = perPage)
            .mapCatching { response ->
                response.photos.photo
                    .map { flickrPhoto -> flickrPhoto.toPhotoDomainModel() }
                    .toPersistentList()
            }.applyPhotoResult()
    }

    private suspend fun getRecentPhotos(page: Int, perPage: Int = PAGE_SIZE) {
        startLoading(page)
        photosRepository.getRecentPhotos(page = page, perPage = perPage)
            .mapCatching {
                it.photos.photo
                    .map { flickrPhoto -> flickrPhoto.toPhotoDomainModel() }
                    .toPersistentList()
            }.applyPhotoResult()
    }

    private fun Result<PersistentList<PhotoDomainModel>>.applyPhotoResult() {
        val asyncResult = toAsync()
        val newList = (asyncResult as? Async.Success)?.let {
            state.value.allPhotos
                .plus(it.result)
                .distinctBy { photo -> photo.id }
                .toPersistentList()
        } ?: state.value.allPhotos

        setState {
            copy(
                loadPhotosResult = asyncResult,
                allPhotos = newList
            )
        }
    }


    fun onSubmitClicked() = viewModelScope.launch {
        setState { copy(currentPage = 0) }
        fetchNextPage()
    }

    fun onQueryChange(newValue: String) {
        setState { copy(queryInput = newValue) }
    }

    fun onPhotoGridScrollChanged(
        firstVisibleIndex: Int,
        lastVisibleIndex: Int
    ) {
        if (state.value.lastScrollPosition != firstVisibleIndex) {
            setState { copy(lastScrollPosition = firstVisibleIndex) }
        }

        // Trigger next page load when user has scrolled into the last half of the current page
        val total = state.value.allPhotos.size
        val threshold = total - (PAGE_SIZE / 2)

        if (lastVisibleIndex >= threshold && threshold > 0) {
            Timber.d("Approaching end of page ${state.value.currentPage}, fetching next page...")

            //debounce loading the next page to avoid spamming requests when scrolling quickly
            debounceChannel.trySend { fetchNextPage() }
        }
    }

    companion object {
        const val PAGE_SIZE: Int = 100
        const val PAGE_REFRESH_DELAY_MILLIS = 1000L
    }
}
