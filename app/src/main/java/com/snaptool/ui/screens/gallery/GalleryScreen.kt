package com.snaptool.ui.screens.gallery

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.snaptool.domain.model.MediaItem
import com.snaptool.domain.model.MediaType
import com.snaptool.ui.theme.*
import com.snaptool.viewmodel.GalleryViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onBack: () -> Unit,
    onNavigateToPreview: (String) -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val items by viewModel.mediaItems.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface0)
    ) {
        // Decorative top orb
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-80).dp)
                .background(
                    Brush.radialGradient(listOf(Violet60.copy(alpha = 0.2f), Color.Transparent)),
                    CircleShape
                )
                .blur(60.dp)
        )

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // ── Top bar ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceGlass)
                        .border(1.dp, OutlineGlass, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Indigo80)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text  = "Gallery",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFFE0E0F8),
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (items.isNotEmpty()) {
                        Text(
                            text  = "${items.size} items",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF7777AA)
                        )
                    }
                }
            }

            // ── Content ───────────────────────────────────────────────────
            AnimatedContent(
                targetState = items.isEmpty(),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "galleryContent"
            ) { isEmpty ->
                if (isEmpty) {
                    // Empty state
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Surface2),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint     = Color(0xFF5555AA),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Text(
                                text  = "Nothing here yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFBBBBDD)
                            )
                            Text(
                                text  = "Capture photos, videos, or screen recordings\nand they will appear here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6666AA),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns               = GridCells.Fixed(3),
                        modifier              = Modifier.fillMaxSize(),
                        contentPadding        = PaddingValues(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement   = Arrangement.spacedBy(2.dp)
                    ) {
                        items(items) { item ->
                            GalleryItem(
                                item    = item,
                                onClick = {
                                    val encodedUri = URLEncoder.encode(item.uri.toString(), StandardCharsets.UTF_8.toString())
                                    onNavigateToPreview(encodedUri)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryItem(item: MediaItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        Image(
            painter          = rememberAsyncImagePainter(item.uri),
            contentDescription = null,
            modifier         = Modifier.fillMaxSize(),
            contentScale     = ContentScale.Crop
        )

        // Subtle inner shadow overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                        startY = 0.4f
                    )
                )
        )

        if (item.type == MediaType.VIDEO) {
            // Play badge
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint     = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Video label bottom-left
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text  = "VID",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
