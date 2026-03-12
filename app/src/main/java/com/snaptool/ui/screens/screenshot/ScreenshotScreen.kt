package com.snaptool.ui.screens.screenshot

import android.content.Context
import android.media.projection.MediaProjectionManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.snaptool.ui.theme.*

@Composable
fun ScreenshotScreen(
    onLaunchProjection: (android.content.Intent, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    LaunchedEffect(Unit) {
        onLaunchProjection(projectionManager.createScreenCaptureIntent(), false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface0)
            .statusBarsPadding()
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Surface2)
                    .border(1.dp, Color(0xFF252550), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Indigo80)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Screenshot",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // ── Content ──────────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Indigo50.copy(0.9f), Indigo30))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Screenshot,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Capturing screenshot…",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Please grant screen capture permission",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7777AA)
                    )
                }

                CircularProgressIndicator(
                    color = Indigo60,
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 2.dp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = Color(0xFF9999CC))
                }
            }
        }
    }
}
