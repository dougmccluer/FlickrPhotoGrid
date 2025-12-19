package com.example.fauxtoes.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.compose.koinViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fauxtoes.R
import com.example.fauxtoes.flickr.FlickrPhotoSize
import com.example.fauxtoes.data.PhotoDomainModel
import com.example.fauxtoes.ui.theme.FauxToesTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
    goToDetail: (PhotoDomainModel) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    FauxToesTheme {
        HomeScreenContent(
            state = state as IHomeScreenContentModel,
            onQueryChange = viewModel::onQueryChange,
            onSearchSubmit = viewModel::onSubmitClicked,
            onScrollChanged = viewModel::onPhotoGridScrollChanged,
            onPhotoClick = goToDetail,
            modifier = modifier,
        )
    }
}

@Composable
fun HomeScreenContent(
    state: IHomeScreenContentModel,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onScrollChanged: (Int, Int) -> Unit,
    onPhotoClick: (PhotoDomainModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    var textState by remember {
        mutableStateOf(
            TextFieldValue(text = state.queryInput),
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = textState,
                    maxLines = 1,
                    onValueChange = {
                        textState = it
                        onQueryChange(it.text)
                    },
                    placeholder = { Text(text = "search for photos") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { onSearchSubmit() }
                    ),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onSearchSubmit,
                    modifier = Modifier.alignByBaseline(),
                    enabled = state.searchEnabled
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Search"
                    )
                }
            }
            state.feedState.let {
                when (it) {
                    FeedState.Empty -> {}
                    FeedState.Loading -> LoadingView()
                    is FeedState.Error -> ErrorView(error = it.error)
                    is FeedState.PhotoGrid -> PhotoGrid(
                        photos = it.photos,
                        shouldShowLoadingIndicator = it.shouldShowLoadingIndicator,
                        onScrollChanged = onScrollChanged,
                        onPhotoClick = onPhotoClick,
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun ErrorView(
    error: Throwable,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        //TODO: user-oriented messaging
        Text(
            text = "Error: ${error.message}",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun PhotoGrid(
    photos: ImmutableList<PhotoDomainModel>,
    shouldShowLoadingIndicator: Boolean,
    onScrollChanged: (Int, Int) -> Unit,
    onPhotoClick: (PhotoDomainModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState) {
        snapshotFlow {
            val firstIndex = gridState.firstVisibleItemIndex
            val lastIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            Pair(firstIndex, lastIndex)
        }
            .distinctUntilChanged()
            .collect { (firstIndex, lastIndex) ->
                onScrollChanged(firstIndex, lastIndex)
            }
    }

    Column {
        AnimatedVisibility(visible = shouldShowLoadingIndicator) {
            LinearProgressIndicator(modifier = Modifier
                .height(4.dp)
                .fillMaxWidth())
        }

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(count = 3),
            horizontalArrangement = spacedBy(12.dp),
            verticalArrangement = spacedBy(12.dp),
            modifier = modifier,
        ) {
            itemsIndexed(items = photos) { _, photo ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photo.url(FlickrPhotoSize.SMALL_400))
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.brokenimg)
                        .crossfade(200)
                        .build(),
                    contentDescription = photo.title ?: stringResource(R.string.photo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onPhotoClick(photo) },
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewMainScreenContent_Loaded() {
    FauxToesTheme {
        HomeScreenContent(
            state = object : IHomeScreenContentModel {
                override val searchEnabled = true
                override val queryInput = ""
                override val feedState = FeedState.PhotoGrid(
                    shouldShowLoadingIndicator = true,
                    photos = persistentListOf(
                        PhotoDomainModel(
                            id = "1",
                            server = "dummyServer1",
                            secret = "dummySecret1",
                            title = "P1"
                        ),
                        PhotoDomainModel(
                            id = "2",
                            server = "dummyServer2",
                            secret = "dummySecret2",
                            title = "P2"
                        ),
                    )
                )
            },
            onQueryChange = {},
            onSearchSubmit = {},
            onScrollChanged = { _, _ -> },
            onPhotoClick = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewMainScreenContent_Loading() {
    FauxToesTheme {
        HomeScreenContent(
            state = object : IHomeScreenContentModel {
                override val searchEnabled = true
                override val queryInput = ""
                override val feedState = FeedState.Loading
            },
            onQueryChange = {},
            onSearchSubmit = {},
            onScrollChanged = { _, _ -> },
            onPhotoClick = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewMainScreenContent_Error() {
    FauxToesTheme {
        HomeScreenContent(
            state = object : IHomeScreenContentModel {
                override val searchEnabled = true
                override val queryInput = ""
                override val feedState = FeedState.Error(Throwable("failed to retrieve photos"))
            },
            onQueryChange = {},
            onSearchSubmit = {},
            onScrollChanged = { _, _ -> },
            onPhotoClick = {},
        )
    }
}