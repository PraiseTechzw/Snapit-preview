package com.snaptool.ui.screens.preview

import android.content.Intent
import android.net.Uri
import android.widget.VideoView
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.snaptool.ui.theme.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    uri: String,
    onBack: () -> Unit
) {
    val context    = LocalContext.current
    val decodedUri = Uri.parse(URLDecoder.decode(uri, StandardCharsets.UTF_8.toString()))
    val isVideo    = decodedUri.toString().contains("video") || decodedUri.toString().endsWith(".mp4")

    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ── Media content ──────────────────────────────────────────────────
        if (isVideo) {
            AndroidView(
                factory  = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(decodedUri)
                        start()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model             = decodedUri,
                contentDescription = null,
                modifier          = Modifier.fillMaxSize(),
                contentScale      = ContentScale.Fit
            )
        }

        // ── Top gradient scrim ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent))
                )
        )

        // ── Bottom gradient scrim + actions ───────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)))
                )
        )

        // ── Top bar ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            GlassIconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(22.dp))
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text  = if (isVideo) "VIDEO" else "PHOTO",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isVideo) Cyan70 else Violet70,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ── Bottom action bar ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Share
            ActionButton(
                icon    = Icons.Default.Share,
                label   = "Share",
                tint    = Cyan60,
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = if (isVideo) "video/mp4" else "image/jpeg"
                        putExtra(Intent.EXTRA_STREAM, decodedUri)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Media"))
                }
            )

            // Delete
            ActionButton(
                icon    = Icons.Default.Delete,
                label   = "Delete",
                tint    = ErrorRed,
                onClick = { showDeleteDialog = true }
            )
        }
    }

    // ── Delete confirmation dialog ─────────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor   = Surface2,
            titleContentColor = Color(0xFFE0E0F8),
            textContentColor  = Color(0xFF9999CC),
            title = {
                Text("Delete Media?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This action cannot be undone.", style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        context.contentResolver.delete(decodedUri, null, null)
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text("Delete", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color(0xFF8888BB))
                }
            }
        )
    }
}

@Composable
private fun GlassIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    IconButton(
        onClick  = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
    ) {
        content()
    }
}

@Composable
private fun ActionButton(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        IconButton(
            onClick  = onClick,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(tint.copy(alpha = 0.15f))
                .border(1.dp, tint.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(26.dp))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFFAAAAAA))
    }
}
