package com.snaptool.ui.screens.screenrecord

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.snaptool.domain.model.RecorderState
import com.snaptool.viewmodel.ScreenRecordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenRecordScreen(
    onBack: () -> Unit,
    viewModel: ScreenRecordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val recorderState by viewModel.recorderState.collectAsState()
    val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    val projectionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            viewModel.startRecording(result.resultCode, result.data!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Screen Recording") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (recorderState == RecorderState.RECORDING_SCREEN) Icons.Default.StopCircle else Icons.Default.Videocam,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = if (recorderState == RecorderState.RECORDING_SCREEN) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (recorderState == RecorderState.RECORDING_SCREEN) "Recording Screen..." else "Ready to Record",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Capture your screen and audio smoothly.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (recorderState == RecorderState.RECORDING_SCREEN) {
                Button(
                    onClick = { viewModel.stopRecording() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Stop Recording")
                }
            } else {
                Button(
                    onClick = {
                        projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Start Recording")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Microphone audio will be captured", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
