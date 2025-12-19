package com.example.fauxtoes.ui.photodetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fauxtoes.R
import com.example.fauxtoes.data.PhotoDomainModel
import com.example.fauxtoes.flickr.FlickrComments
import com.example.fauxtoes.flickr.FlickrDates
import com.example.fauxtoes.flickr.FlickrOwner
import com.example.fauxtoes.flickr.FlickrPhotoInfo
import com.example.fauxtoes.flickr.FlickrPhotoSize
import com.example.fauxtoes.flickr.FlickrPublicEditability
import com.example.fauxtoes.flickr.FlickrTextContent
import com.example.fauxtoes.flickr.FlickrUsage
import com.example.fauxtoes.flickr.FlickrVisibility
import com.example.fauxtoes.ui.theme.FauxToesTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailScreen(
    photo: PhotoDomainModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhotoDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(photo.id) {
        viewModel.loadPhotoInfo(photoId = photo.id, secret = photo.secret)
    }

    PhotoDetailsScaffold(
        state = state,
        photo = photo,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun PhotoDetailsScaffold(
    photo: PhotoDomainModel,
    state: PhotoDetailState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) =  Scaffold(
    topBar = {
        TopAppBar(
            title = {
                Text(
                    text = photo.title ?: stringResource(R.string.photo),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        )
    },
    modifier = modifier
) { innerPadding ->
    when (state) {
        is PhotoDetailState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is PhotoDetailState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (state as PhotoDetailState.Error).message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        is PhotoDetailState.Success -> {
            PhotoDetailContent(
                photo = photo,
                photoInfo = (state as PhotoDetailState.Success).photoInfo,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun PhotoDetailContent(
    photo: PhotoDomainModel,
    photoInfo: FlickrPhotoInfo,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.url(FlickrPhotoSize.LARGE_1024))
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.brokenimg)
                .crossfade(200)
                .build(),
            contentDescription = photoInfo.title?.content,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = photoInfo?.title?.content ?: "",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center
        )

        if (photoInfo.description?.content?.isNotBlank() == true) {
            val htmlDescription = remember(photoInfo.description?.content) {
                val spanned = HtmlCompat.fromHtml(
                    photoInfo.description?.content ?: "",
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
                buildAnnotatedString {
                    append(spanned.toString())
                }
            }

            Text(
                text = htmlDescription,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Text(
            text = "By ${photoInfo.owner?.username ?: ""}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (photoInfo.owner?.realname?.isNotBlank() == true) {
            Text(
                text = "Real name: ${photoInfo?.owner?.realname}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
        }

        photoInfo.views?.let { views ->
            Text(
                text = "Views: $views",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        photoInfo.tags?.tag?.takeIf { it.isNotEmpty() }?.let { tags ->
            Text(
                text = "Tags: ${tags.joinToString(", ") { it.content }}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        photoInfo.dates?.taken?.let { taken ->
            Text(
                text = "Taken: $taken",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPhotoDetailsScaffoldLoading() {
    FauxToesTheme {
        PhotoDetailsScaffold(
            photo = PhotoDomainModel(
                id = "12345",
                server = "65535",
                secret = "abc123",
                title = "Beautiful Sunset"
            ),
            state = PhotoDetailState.Loading,
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPhotoDetailsScaffoldError() {
    FauxToesTheme {
        PhotoDetailsScaffold(
            photo = PhotoDomainModel(
                id = "12345",
                server = "65535",
                secret = "abc123",
                title = "Beautiful Sunset"
            ),
            state = PhotoDetailState.Error("Failed to load photo details"),
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPhotoDetailsScaffoldSuccess() {
    FauxToesTheme {
        PhotoDetailsScaffold(
            photo = PhotoDomainModel(
                id = "12345",
                server = "65535",
                secret = "abc123",
                title = "Beautiful Sunset"
            ),
            state = PhotoDetailState.Success(
                FlickrPhotoInfo(
                    id = "12345",
                    secret = "abc123",
                    server = "65535",
                    farm = 66,
                    dateUploaded = "1702992000",
                    isFavorite = false,
                    license = "0",
                    safetyLevel = "0",
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
                    description = FlickrTextContent(content = "A stunning sunset over the ocean with vibrant colors"),
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
            ),
            onNavigateBack = {}
        )
    }
}
