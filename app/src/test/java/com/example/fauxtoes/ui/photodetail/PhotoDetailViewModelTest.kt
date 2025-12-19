package com.example.fauxtoes.ui.photodetail

import com.example.fauxtoes.flickr.FlickrApiService
import com.example.fauxtoes.flickr.FlickrComments
import com.example.fauxtoes.flickr.FlickrDates
import com.example.fauxtoes.flickr.FlickrOwner
import com.example.fauxtoes.flickr.FlickrPhotoInfo
import com.example.fauxtoes.flickr.FlickrPhotoInfoResponse
import com.example.fauxtoes.flickr.FlickrPublicEditability
import com.example.fauxtoes.flickr.FlickrTextContent
import com.example.fauxtoes.flickr.FlickrUsage
import com.example.fauxtoes.flickr.FlickrVisibility
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoDetailViewModelTest {

    private lateinit var viewModel: PhotoDetailViewModel
    private lateinit var flickrApiService: FlickrApiService
    private val testDispatcher = StandardTestDispatcher()

    private val mockPhotoInfo = FlickrPhotoInfo(
        id = "12345",
        secret = "abc123",
        server = "65535",
        farm = 66,
        dateUploaded = "1702992000",
        isFavorite = false,
        license = null,
        safetyLevel = null,
        rotation = 0,
        owner = FlickrOwner(
            nsid = "123456@N01",
            username = "johndoe",
            realname = "John Doe",
            location = "San Francisco, CA",
            iconServer = "1",
            iconFarm = 1,
            pathAlias = "johndoe"
        ),
        title = FlickrTextContent(content = "Beautiful Sunset"),
        description = FlickrTextContent(content = "A stunning sunset"),
        visibility = FlickrVisibility(isPublic = 1, isFriend = 0, isFamily = 0),
        dates = FlickrDates(
            posted = "1702992000",
            taken = "2024-12-19 18:30:00",
            takenGranularity = 0,
            takenUnknown = "0",
            lastUpdate = "1702992000"
        ),
        views = "1234",
        editability = null,
        publicEditability = FlickrPublicEditability(canComment = 0, canAddMeta = 0),
        usage = FlickrUsage(canDownload = 1, canBlog = 1, canPrint = 1, canShare = 1),
        comments = FlickrComments(content = "0"),
        notes = null,
        people = null,
        tags = null,
        urls = null,
        media = "photo"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        flickrApiService = mockk(relaxed = true)
        viewModel = PhotoDetailViewModel(flickrApiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsLoading() {
        val state = viewModel.state.value
        assertTrue(state is PhotoDetailState.Loading)
    }

    @Test
    fun loadPhotoInfoSuccessUpdatesStateToSuccess() = runTest {
        coEvery {
            flickrApiService.getInfo(photoId = "12345", secret = "abc123")
        } returns Result.success(FlickrPhotoInfoResponse(photo = mockPhotoInfo))

        viewModel.loadPhotoInfo(photoId = "12345", secret = "abc123")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is PhotoDetailState.Success)
        assertEquals(mockPhotoInfo, (state as PhotoDetailState.Success).photoInfo)
    }

    @Test
    fun loadPhotoInfoFailureUpdatesStateToError() = runTest {
        val errorMessage = "Network error"
        coEvery {
            flickrApiService.getInfo(photoId = "12345", secret = "abc123")
        } returns Result.failure(Exception(errorMessage))

        viewModel.loadPhotoInfo(photoId = "12345", secret = "abc123")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is PhotoDetailState.Error)
        assertEquals(errorMessage, (state as PhotoDetailState.Error).message)
    }

    @Test
    fun loadPhotoInfoWithNullMessageUsesDefaultErrorMessage() = runTest {
        coEvery {
            flickrApiService.getInfo(photoId = "12345", secret = "abc123")
        } returns Result.failure(Exception())

        viewModel.loadPhotoInfo(photoId = "12345", secret = "abc123")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is PhotoDetailState.Error)
        assertEquals("Failed to load photo details", (state as PhotoDetailState.Error).message)
    }

    @Test
    fun loadPhotoInfoSetsLoadingStateBeforeApiCall() = runTest {
        coEvery {
            flickrApiService.getInfo(photoId = "12345", secret = "abc123")
        } coAnswers {
            delay(100)
            Result.success(FlickrPhotoInfoResponse(photo = mockPhotoInfo))
        }

        viewModel.loadPhotoInfo(photoId = "12345", secret = "abc123")

        val state = viewModel.state.value
        assertTrue(state is PhotoDetailState.Loading)
    }

    @Test
    fun loadPhotoInfoCallsApiServiceWithCorrectParameters() = runTest {
        coEvery {
            flickrApiService.getInfo(photoId = "12345", secret = "abc123")
        } returns Result.success(FlickrPhotoInfoResponse(photo = mockPhotoInfo))

        viewModel.loadPhotoInfo(photoId = "12345", secret = "abc123")
        advanceUntilIdle()

        coVerify {
            flickrApiService.getInfo(photoId = "12345", secret = "abc123")
        }
    }

    @Test
    fun loadPhotoInfoWithDifferentPhotoIdCallsApiWithNewParameters() = runTest {
        coEvery {
            flickrApiService.getInfo(photoId = any(), secret = any())
        } returns Result.success(FlickrPhotoInfoResponse(photo = mockPhotoInfo))

        viewModel.loadPhotoInfo(photoId = "99999", secret = "xyz789")
        advanceUntilIdle()

        coVerify {
            flickrApiService.getInfo(photoId = "99999", secret = "xyz789")
        }
    }

    @Test
    fun subsequentLoadPhotoInfoCallsOverwritePreviousState() = runTest {
        coEvery {
            flickrApiService.getInfo(photoId = "12345", secret = "abc123")
        } returns Result.success(FlickrPhotoInfoResponse(photo = mockPhotoInfo))

        viewModel.loadPhotoInfo(photoId = "12345", secret = "abc123")
        advanceUntilIdle()

        val firstState = viewModel.state.value
        assertTrue(firstState is PhotoDetailState.Success)

        val errorMessage = "Second call failed"
        coEvery {
            flickrApiService.getInfo(photoId = "67890", secret = "def456")
        } returns Result.failure(Exception(errorMessage))

        viewModel.loadPhotoInfo(photoId = "67890", secret = "def456")
        advanceUntilIdle()

        val secondState = viewModel.state.value
        assertTrue(secondState is PhotoDetailState.Error)
        assertEquals(errorMessage, (secondState as PhotoDetailState.Error).message)
    }

    @Test
    fun loadPhotoInfoHandlesMinimalPhotoInfoWithNullableFields() = runTest {
        val minimalPhotoInfo = FlickrPhotoInfo(
            id = "12345",
            secret = "abc123",
            server = "65535",
            farm = 66,
            dateUploaded = null,
            isFavorite = null,
            license = null,
            safetyLevel = null,
            rotation = null,
            owner = FlickrOwner(
                nsid = "mock owner",
                username = null,
                realname = null,
                location = null,
                iconServer = null,
                iconFarm = null,
                pathAlias = null,
            ),
            title = null,
            description = null,
            visibility = FlickrVisibility(isPublic = 1, isFriend = 0, isFamily = 0),
            dates = FlickrDates(
                posted = "1702992000",
                taken = "2024-12-19 18:30:00",
                takenGranularity = 0,
                takenUnknown = "0",
                lastUpdate = "1702992000"
            ),
            views = "100",
            editability = null,
            publicEditability = FlickrPublicEditability(canComment = 0, canAddMeta = 0),
            usage = FlickrUsage(canDownload = 1, canBlog = 1, canPrint = 1, canShare = 1),
            comments = FlickrComments(content = null),
            notes = null,
            people = null,
            tags = null,
            urls = null,
            media = null
        )

        coEvery {
            flickrApiService.getInfo(photoId = "12345", secret = "abc123")
        } returns Result.success(FlickrPhotoInfoResponse(photo = minimalPhotoInfo))

        viewModel.loadPhotoInfo(photoId = "12345", secret = "abc123")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is PhotoDetailState.Success)
        assertEquals(minimalPhotoInfo, (state as PhotoDetailState.Success).photoInfo)
    }
}

