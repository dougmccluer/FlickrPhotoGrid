package com.example.fauxtoes.ui

import com.example.fauxtoes.core.Async
import com.example.fauxtoes.data.PhotosRepository
import com.example.fauxtoes.flickr.FlickrPhoto
import com.example.fauxtoes.flickr.FlickrPhotosPage
import com.example.fauxtoes.flickr.FlickrRecentPhotosResponse
import com.example.fauxtoes.flickr.FlickrSearchPhotosResponse
import com.example.fauxtoes.ui.home.HomeViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var photosRepository: PhotosRepository
    private val testDispatcher = StandardTestDispatcher()

    private fun getMockFlickrPhoto(id:Int) = FlickrPhoto(
        id = "$id",
        server = "server",
        secret = "secret",
        title = "Photo $id",
        owner = "owner",
        farm = 1,
        isPublic = true,
        isFriend = false,
        isFamily = false
    )
    private val mockPhotos = List(100){ getMockFlickrPhoto(it) }
    private fun photoPageResponse(page:Int) = FlickrPhotosPage(
        page = page,
        pages = 100,
        perpage = 100,
        total = 999,
        photo = mockPhotos
    )

    private fun recentPhotoResponse(page:Int = 1) = FlickrRecentPhotosResponse(
        photos = photoPageResponse(page)
    )

    private fun searchPhotoResponse(page:Int = 1) = FlickrSearchPhotosResponse(
        photos = photoPageResponse(page)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        photosRepository = mockk(relaxed = true)
        viewModel = HomeViewModel(photosRepository)

        coEvery {
            photosRepository.getRecentPhotos(1, any())
        } answers{ Result.success(recentPhotoResponse(1)) }
        coEvery {
            photosRepository.getRecentPhotos(2, any())
        } answers{ Result.success(recentPhotoResponse(1)) }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun scrollChangedUpdatesLastScrollPosition() = runTest(testDispatcher) {
        val initialState = viewModel.state.value
        assertEquals(0, initialState.lastScrollPosition)

        viewModel.onPhotoGridScrollChanged(firstVisibleIndex = 5, lastVisibleIndex = 10)

        val updatedState = viewModel.state.value
        assertEquals(5, updatedState.lastScrollPosition)
    }

    @Test
    fun scrollChangedDoesNotUpdateWhenFirstVisibleIndexUnchanged() = runTest(testDispatcher) {
        viewModel.onPhotoGridScrollChanged(firstVisibleIndex = 5, lastVisibleIndex = 10)
        val stateAfterFirst = viewModel.state.value

        viewModel.onPhotoGridScrollChanged(firstVisibleIndex = 5, lastVisibleIndex = 15)
        val stateAfterSecond = viewModel.state.value

        assertEquals(5, stateAfterFirst.lastScrollPosition)
        assertEquals(5, stateAfterSecond.lastScrollPosition)
    }

    @Test
    fun scrollToEndTriggersNextPageLoad() = runTest(testDispatcher) {


        viewModel.onSubmitClicked()
        advanceUntilIdle()

        val initialPage = viewModel.state.value.currentPage
        val photosCount = viewModel.state.value.allPhotos.size

        viewModel.onPhotoGridScrollChanged(
            firstVisibleIndex = 40,
            lastVisibleIndex = photosCount - 45
        )

        advanceTimeBy(HomeViewModel.PAGE_REFRESH_DELAY_MILLIS + 100)
        advanceUntilIdle()

        assertNotEquals(initialPage, viewModel.state.value.currentPage)
    }

    @Test
    fun scrollBeforeThresholdDoesNotTriggerNextPageLoad() = runTest(testDispatcher) {

        viewModel.onSubmitClicked()
        advanceUntilIdle()

        val initialPage = viewModel.state.value.currentPage

        viewModel.onPhotoGridScrollChanged(
            firstVisibleIndex = 0,
            lastVisibleIndex = 10
        )

        advanceTimeBy(HomeViewModel.PAGE_REFRESH_DELAY_MILLIS + 100)
        advanceUntilIdle()

        assertEquals(initialPage, viewModel.state.value.currentPage)
    }

    @Test
    fun scrollChangedWithEmptyPhotosDoesNotTriggerLoad() = runTest(testDispatcher) {
        val initialState = viewModel.state.value
        assertEquals(0, initialState.allPhotos.size)

        viewModel.onPhotoGridScrollChanged(
            firstVisibleIndex = 0,
            lastVisibleIndex = 50
        )

        advanceTimeBy(HomeViewModel.PAGE_REFRESH_DELAY_MILLIS + 100)
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.allPhotos.size)
    }

    @Test
    fun scrollChangedDebouncesMultipleRequests() = runTest(testDispatcher) {
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        val photosCount = viewModel.state.value.allPhotos.size
        val threshold = photosCount - (HomeViewModel.PAGE_SIZE / 2)

        viewModel.onPhotoGridScrollChanged(
            firstVisibleIndex = threshold - 5,
            lastVisibleIndex = threshold + 1
        )

        advanceTimeBy(100)

        viewModel.onPhotoGridScrollChanged(
            firstVisibleIndex = threshold - 3,
            lastVisibleIndex = threshold + 2
        )

        advanceTimeBy(100)

        viewModel.onPhotoGridScrollChanged(
            firstVisibleIndex = threshold - 1,
            lastVisibleIndex = threshold + 3
        )

        advanceTimeBy(HomeViewModel.PAGE_REFRESH_DELAY_MILLIS + 100)
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.currentPage)
    }

    @Test
    fun scrollChangedAtExactThresholdTriggersLoad() = runTest(testDispatcher) {

        viewModel.onSubmitClicked()
        advanceUntilIdle()

        val photosCount = viewModel.state.value.allPhotos.size
        val threshold = photosCount - (HomeViewModel.PAGE_SIZE / 2)

        viewModel.onPhotoGridScrollChanged(
            firstVisibleIndex = 0,
            lastVisibleIndex = threshold
        )

        advanceTimeBy(HomeViewModel.PAGE_REFRESH_DELAY_MILLIS + 100)
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.currentPage)
    }

    @Test
    fun scrollChangedWhileLoadingDoesNotTriggerDuplicateLoad() = runTest(testDispatcher) {
        coEvery {
            photosRepository.getRecentPhotos(any(), any())
        } coAnswers {
            delay(5000)
            Result.success(mockk(relaxed = true) {
                coEvery { photos.photo } returns mockPhotos
            })
        }

        viewModel.onSubmitClicked()
        advanceTimeBy(100)

        assertEquals(Async.Loading, viewModel.state.value.loadPhotosResult)

        val photosCount = 100
        val threshold = photosCount - (HomeViewModel.PAGE_SIZE / 2)

        viewModel.onPhotoGridScrollChanged(
            firstVisibleIndex = 0,
            lastVisibleIndex = threshold + 10
        )

        advanceTimeBy(HomeViewModel.PAGE_REFRESH_DELAY_MILLIS + 100)

        assertEquals(1, viewModel.state.value.currentPage)
    }
}

