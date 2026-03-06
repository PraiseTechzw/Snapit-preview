package com.snaptool.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.ripple.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.snaptool.domain.model.RecorderState
import com.snaptool.ui.theme.*
import com.snaptool.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
        onNavigateToCamera: () -> Unit,
        onNavigateToScreenRecord: () -> Unit,
        onNavigateToGallery: () -> Unit,
        onNavigateToSettings: () -> Unit,
        viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val recorderState by viewModel.recorderState.collectAsState()

    var cameraPermissionGranted by remember {
        mutableStateOf(
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
            rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                cameraPermissionGranted =
                        permissions[Manifest.permission.CAMERA] ?: cameraPermissionGranted
            }

    LaunchedEffect(Unit) {
        val permissions =
                mutableListOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions +=
                    listOf(
                            Manifest.permission.POST_NOTIFICATIONS,
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO
                    )
        } else {
            permissions += Manifest.permission.READ_EXTERNAL_STORAGE
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val gradientOffset by
            infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec =
                            infiniteRepeatable(
                                    animation = tween(8000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                            ),
                    label = "gradOffset"
            )

    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .background(
                                    Brush.radialGradient(
                                            colors =
                                                    listOf(
                                                            Color(0xFF1B1B45)
                                                                    .copy(
                                                                            alpha =
                                                                                    0.8f +
                                                                                            gradientOffset *
                                                                                                    0.2f
                                                                    ),
                                                            Surface0
                                                    ),
                                            center = Offset(0.3f + gradientOffset * 0.4f, 0.2f),
                                            radius = 1800f
                                    )
                            )
    ) {
        // Decorative blurred orb
        Box(
                modifier =
                        Modifier.size(280.dp)
                                .offset(x = (-60).dp, y = (-40).dp)
                                .background(
                                        Brush.radialGradient(
                                                listOf(
                                                        Indigo50.copy(alpha = 0.35f),
                                                        Color.Transparent
                                                )
                                        ),
                                        shape = RoundedCornerShape(50)
                                )
                                .blur(60.dp)
        )
        Box(
                modifier =
                        Modifier.size(200.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 40.dp, y = 40.dp)
                                .background(
                                        Brush.radialGradient(
                                                listOf(
                                                        Violet60.copy(alpha = 0.25f),
                                                        Color.Transparent
                                                )
                                        ),
                                        shape = RoundedCornerShape(50)
                                )
                                .blur(50.dp)
        )

        Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // ── Header ────────────────────────────────────────────────────
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                            text = "Snapit",
                            style =
                                    MaterialTheme.typography.headlineLarge.copy(
                                            brush =
                                                    Brush.linearGradient(
                                                            listOf(Indigo80, Violet70)
                                                    ),
                                            fontWeight = FontWeight.ExtraBold
                                    )
                    )
                    Text(
                            text = "Capture. Record. Create.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8888BB)
                    )
                }
                IconButton(
                        onClick = onNavigateToSettings,
                        modifier =
                                Modifier.size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceGlass)
                                        .border(1.dp, OutlineGlass, RoundedCornerShape(12.dp))
                ) { Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Indigo80) }
            }

            // ── Status & Dash Row ──────────────────────────────────────────
            Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusPill(recorderState, modifier = Modifier.weight(1f))

                // Stats Card (Premium utility look)
                Box(
                        modifier =
                                Modifier.weight(1.2f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(SurfaceGlass)
                                        .border(1.dp, OutlineGlass, RoundedCornerShape(20.dp))
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(
                                    "128",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                            )
                            Text(
                                    "CAPTURES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Indigo80
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                                Icons.Default.CloudQueue,
                                null,
                                tint = Indigo80.copy(0.4f),
                                modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ── Recent Media Carousel ────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Last Captures", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Gallery", style = MaterialTheme.typography.labelSmall, color = Indigo80, modifier = Modifier.clickable { onNavigateToGallery() })
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Quick placeholder items with better style
                    listOf(Icons.Default.Photo, Icons.Default.Videocam, Icons.Default.Collections).forEach { icon ->
                        Box(
                            modifier = Modifier
                                .size(width = 100.dp, height = 75.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Surface2)
                                .border(1.dp, OutlineGlass, RoundedCornerShape(16.dp))
                                .alpha(0.7f),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, null, tint = Indigo80.copy(0.4f), modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Power Mode Discovery ──────────────────────────────────────
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                            Brush.horizontalGradient(
                                                    listOf(Indigo50.copy(0.2f), Violet60.copy(0.1f))
                                            )
                                    )
                                    .border(1.dp, Indigo50.copy(0.3f), RoundedCornerShape(24.dp))
                                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(Indigo50),
                            contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.AutoMode, null, tint = Color.White) }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                                "Power Mode",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                        )
                        Text(
                                "Enable overlay for background capture",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFAAAAAA)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                            onClick = onNavigateToSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo50),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) { Text("SETUP", style = MaterialTheme.typography.labelMedium) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Primary action card ───────────────────────────────────────
            PrimaryActionCard(
                    icon = Icons.Default.CameraAlt,
                    title = "Smart Camera",
                    subtitle = "Pro-grade Photo & Video",
                    gradient = Brush.linearGradient(listOf(Indigo50, Violet60)),
                    onClick = onNavigateToCamera,
                    enabled = cameraPermissionGranted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Secondary card row ────────────────────────────────────────
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SecondaryActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Screenshot,
                        title = "Record\nScreen",
                        accentColor = Cyan50,
                        onClick = onNavigateToScreenRecord
                )
                SecondaryActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.PhotoLibrary,
                        title = "My\nGallery",
                        accentColor = Violet60,
                        onClick = onNavigateToGallery
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Footer label ──────────────────────────────────────────────
            Text(
                    text = "Snapit v1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF55557A),
                    modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

// ── Status Pill ───────────────────────────────────────────────────────────────
@Composable
private fun StatusPill(recorderState: RecorderState, modifier: Modifier = Modifier) {
    val isRecording =
            recorderState == RecorderState.RECORDING_SCREEN ||
                    recorderState == RecorderState.RECORDING_VIDEO

    val pillColor = if (isRecording) RecordRed else SuccessGreen

    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulse by
            pulseAnim.animateFloat(
                    initialValue = 0.85f,
                    targetValue = 1f,
                    animationSpec =
                            infiniteRepeatable(
                                    animation = tween(900, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                            ),
                    label = "pulseScale"
            )

    Row(
            modifier =
                    modifier.clip(RoundedCornerShape(50))
                            .background(SurfaceGlass)
                            .border(1.dp, OutlineGlass, RoundedCornerShape(50))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
                modifier =
                        Modifier.size(8.dp)
                                .scale(if (isRecording) pulse else 1f)
                                .background(
                                        pillColor,
                                        shape = androidx.compose.foundation.shape.CircleShape
                                )
        )
        Text(
                text = if (isRecording) "● ${recorderState.name.replace('_', ' ')}" else "Ready",
                style = MaterialTheme.typography.labelMedium,
                color = if (isRecording) RecordRed else SuccessGreen
        )
    }
}

// ── Primary Action Card ───────────────────────────────────────────────────────
@Composable
private fun PrimaryActionCard(
        icon: ImageVector,
        title: String,
        subtitle: String,
        gradient: Brush,
        onClick: () -> Unit,
        enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by
            animateFloatAsState(
                    targetValue = if (isPressed) 0.97f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "cardScale"
            )

    Box(
            modifier =
                    Modifier.fillMaxWidth()
                            .height(140.dp)
                            .scale(scale)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                    if (enabled) gradient
                                    else Brush.linearGradient(listOf(Surface2, Surface3))
                            )
                            .clickable(
                                    interactionSource = interactionSource,
                                    indication =
                                            rememberRipple(color = Color.White.copy(alpha = 0.3f)),
                                    enabled = enabled,
                                    onClick = onClick
                            )
    ) {
        // Subtle inner glow stripe
        Box(
                modifier =
                        Modifier.fillMaxWidth(0.5f)
                                .height(1.dp)
                                .align(Alignment.TopCenter)
                                .background(Color.White.copy(alpha = 0.4f))
        )

        Row(
                modifier = Modifier.fillMaxSize().padding(28.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                    modifier =
                            Modifier.size(64.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                )
                Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp)
            )
        }

        if (!enabled) {
            Box(
                    modifier =
                            Modifier.fillMaxSize()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        "Camera permission required",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(0.8f)
                )
            }
        }
    }
}

// ── Secondary Action Card ─────────────────────────────────────────────────────
@Composable
private fun SecondaryActionCard(
        modifier: Modifier = Modifier,
        icon: ImageVector,
        title: String,
        accentColor: Color,
        onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by
            animateFloatAsState(
                    targetValue = if (isPressed) 0.96f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "secCardScale"
            )

    Box(
            modifier =
                    modifier.height(140.dp)
                            .scale(scale)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Surface2)
                            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                            .clickable(
                                    interactionSource = interactionSource,
                                    indication = rememberRipple(color = accentColor.copy(0.2f)),
                                    onClick = onClick
                            )
    ) {
        // Top accent stripe
        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .height(2.dp)
                                .background(
                                        Brush.linearGradient(
                                                listOf(
                                                        accentColor.copy(alpha = 0f),
                                                        accentColor,
                                                        accentColor.copy(alpha = 0f)
                                                )
                                        )
                                )
        )

        Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                    modifier =
                            Modifier.size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                )
            }
            Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFE0E0F0),
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 20.sp
            )
        }
    }
}
