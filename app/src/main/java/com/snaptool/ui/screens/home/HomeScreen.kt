package com.snaptool.ui.screens.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.snaptool.domain.model.RecorderState
import com.snaptool.ui.theme.*
import com.snaptool.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onNavigateToScreenshot: () -> Unit,
    onNavigateToScreenRecord: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val recorderState by viewModel.recorderState.collectAsState()
    val isRecording = recorderState == RecorderState.RECORDING_SCREEN

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {}

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += listOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            permissions += Manifest.permission.READ_EXTERNAL_STORAGE
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface0)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Snapit",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Screenshot & Screen Record",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF7777AA)
                )
            }
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Surface2)
                    .border(1.dp, Color(0xFF252550), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Indigo80)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Status Bar ──────────────────────────────────────────────────────
        if (isRecording) {
            RecordingBanner()
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Primary Action — Screenshot ──────────────────────────────────────
        ActionCard(
            icon = Icons.Default.Screenshot,
            title = "Take Screenshot",
            subtitle = "Capture your current screen instantly",
            gradient = Brush.linearGradient(listOf(Indigo50, Indigo60)),
            onClick = onNavigateToScreenshot
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Primary Action — Screen Record ──────────────────────────────────
        ActionCard(
            icon = Icons.Default.Videocam,
            title = "Record Screen",
            subtitle = "Record your screen with audio",
            gradient = Brush.linearGradient(listOf(Color(0xFF0D7377), Cyan50)),
            onClick = onNavigateToScreenRecord
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Quick Links ──────────────────────────────────────────────────────
        Text(
            text = "QUICK ACCESS",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF555588),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.PhotoLibrary,
                label = "Gallery",
                onClick = onNavigateToGallery
            )
            QuickTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Tune,
                label = "Settings",
                onClick = onNavigateToSettings
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // ── Footer ───────────────────────────────────────────────────────────
        Text(
            text = "Snapit v1.0.0",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 20.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF33334A)
        )
    }
}

@Composable
private fun RecordingBanner() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RecordRed.copy(alpha = 0.12f))
            .border(1.dp, RecordRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(alpha)
                .background(RecordRed, CircleShape)
        )
        Text(
            "Recording in progress",
            style = MaterialTheme.typography.bodySmall,
            color = RecordRed,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun QuickTile(modifier: Modifier, icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Surface2)
            .border(1.dp, Color(0xFF252550), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Indigo80, modifier = Modifier.size(26.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFFB0B0CC))
    }
}
