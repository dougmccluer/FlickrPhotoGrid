package com.example.fauxtoes.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.fauxtoes.data.PhotoDomainModel
import com.example.fauxtoes.ui.home.HomeScreen
import com.example.fauxtoes.ui.photodetail.PhotoDetailScreen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object HomeRoute

@Serializable
data class PhotoDetailRoute(
    val photoId: String,
    val server: String,
    val secret: String,
    val title: String?
)

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        composable<HomeRoute> {
            HomeScreen(
                viewModel = koinViewModel(),
                goToDetail = { photo ->
                    navController.navigate(
                        PhotoDetailRoute(
                            photoId = photo.id,
                            server = photo.server,
                            secret = photo.secret,
                            title = photo.title
                        )
                    )
                }
            )
        }

        composable<PhotoDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<PhotoDetailRoute>()
            PhotoDetailScreen(
                photo = PhotoDomainModel(
                    id = route.photoId,
                    server = route.server,
                    secret = route.secret,
                    title = route.title
                ),
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}

