package com.snaptool.ui.screens.screenshot

import android.content.Context
import android.media.projection.MediaProjectionManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.snaptool.domain.model.RecorderState

@Composable
fun ScreenshotScreen(
    onLaunchProjection: (android.content.Intent, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_MANAGER_SERVICE) as MediaProjectionManager

    LaunchedEffect(Unit) {
        // Automatically request projection on entry if we came from a deep link
        onLaunchProjection(projectionManager.createScreenCaptureIntent(), false)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Capturing Screenshot...",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onBack) {
                Text("Cancel")
            }
        }
    }
}
