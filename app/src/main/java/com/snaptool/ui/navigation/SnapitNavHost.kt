package com.snaptool.ui.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.snaptool.ui.screens.camera.CameraScreen
import com.snaptool.ui.screens.gallery.GalleryScreen
import com.snaptool.ui.screens.home.HomeScreen
import com.snaptool.ui.screens.preview.PreviewScreen
import com.snaptool.ui.screens.screenrecord.ScreenRecordScreen
import com.snaptool.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home         : Screen("home")
    object Camera       : Screen("camera")
    object ScreenRecord : Screen("screen_record")
    object Gallery      : Screen("gallery")
    object Settings     : Screen("settings")
    object Preview      : Screen("preview/{uri}") {
        fun createRoute(uri: String) = "preview/$uri"
    }
}

@Composable
fun SnapitNavHost(
    /** Passed down from MainActivity — launches the system MediaProjection consent dialog. */
    onLaunchProjection: (Intent, Boolean) -> Unit,
    initialUri: android.net.Uri? = null
) {
    val navController = rememberNavController()

    androidx.compose.runtime.LaunchedEffect(initialUri) {
        initialUri?.let { uri ->
            when (uri.host) {
                "camera" -> navController.navigate(Screen.Camera.route)
                "screen_record" -> navController.navigate(Screen.ScreenRecord.route)
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCamera       = { navController.navigate(Screen.Camera.route) },
                onNavigateToScreenRecord = { navController.navigate(Screen.ScreenRecord.route) },
                onNavigateToGallery      = { navController.navigate(Screen.Gallery.route) },
                onNavigateToSettings     = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.ScreenRecord.route) {
            ScreenRecordScreen(
                onBack             = { navController.popBackStack() },
                onLaunchProjection = onLaunchProjection
            )
        }

        composable(Screen.Gallery.route) {
            GalleryScreen(
                onBack              = { navController.popBackStack() },
                onNavigateToPreview = { uri ->
                    navController.navigate(Screen.Preview.createRoute(uri))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Preview.route) { backStackEntry ->
            val uri = backStackEntry.arguments?.getString("uri") ?: ""
            PreviewScreen(uri = uri, onBack = { navController.popBackStack() })
        }
    }
}
