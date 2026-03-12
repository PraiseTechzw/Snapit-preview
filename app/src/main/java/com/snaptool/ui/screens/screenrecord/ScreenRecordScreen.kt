package com.snaptool.ui.screens.screenrecord

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.snaptool.domain.model.RecorderState
import com.snaptool.ui.theme.*
import com.snaptool.viewmodel.ScreenRecordViewModel

@Composable
fun ScreenRecordScreen(
    onBack: () -> Unit,
    onLaunchProjection: (Intent, Boolean) -> Unit,
    viewModel: ScreenRecordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val recorderState by viewModel.recorderState.collectAsState()
    val audioEnabled by viewModel.audioEnabled.collectAsState(initial = true)
    val isRecording = recorderState == RecorderState.RECORDING_SCREEN
    val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

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
                text = "Screen Recording",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // ── Central status icon ───────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        if (isRecording)
                            Brush.radialGradient(listOf(RecordRed.copy(0.8f), Color(0xFF6B0000)))
                        else
                            Brush.radialGradient(listOf(Cyan50.copy(0.9f), Indigo50))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isRecording) "Recording…" else "Ready to Record",
                style = MaterialTheme.typography.headlineSmall,
                color = if (isRecording) RecordRed else Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isRecording)
                    "Your screen is being captured"
                else
                    "Tap the button below to start",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF7777AA)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // ── Info row ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            Chip(icon = Icons.Default.Mic, label = if (audioEnabled) "Audio On" else "No Audio")
            Chip(icon = Icons.Default.Hd, label = "HD Quality")
            Chip(icon = Icons.Default.VideoFile, label = "MP4")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Action button ─────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).navigationBarsPadding().padding(bottom = 32.dp)) {
            if (isRecording) {
                Button(
                    onClick = { viewModel.stopRecording() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RecordRed)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop Recording", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                Button(
                    onClick = { onLaunchProjection(projectionManager.createScreenCaptureIntent(), audioEnabled) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo50)
                ) {
                    Icon(Icons.Default.FiberManualRecord, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Recording", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun Chip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Surface2)
            .border(1.dp, Color(0xFF252550), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Cyan50, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9999BB))
    }
}
