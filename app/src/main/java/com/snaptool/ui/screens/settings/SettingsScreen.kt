package com.snaptool.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.snaptool.ui.theme.*
import com.snaptool.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val screenshotPrefix by viewModel.screenshotPrefix.collectAsState()
    val audioEnabled by viewModel.audioEnabled.collectAsState()
    val recordQuality by viewModel.recordQuality.collectAsState()

    var tempPrefix by remember(screenshotPrefix) { mutableStateOf(screenshotPrefix) }

    Box(modifier = Modifier.fillMaxSize().background(Surface0)) {
        // Accent orb
        Box(
                modifier =
                        Modifier.size(220.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 60.dp, y = (-40).dp)
                                .background(
                                        Brush.radialGradient(
                                                listOf(
                                                        Indigo50.copy(alpha = 0.18f),
                                                        Color.Transparent
                                                )
                                        ),
                                        CircleShape
                                )
                                .blur(60.dp)
        )

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // ── Top bar ───────────────────────────────────────────────────
            Row(
                    modifier =
                            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                        onClick = onBack,
                        modifier =
                                Modifier.size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceGlass)
                                        .border(1.dp, OutlineGlass, RoundedCornerShape(12.dp))
                ) { Icon(Icons.Default.ArrowBack, "Back", tint = Indigo80) }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFFE0E0F8),
                        fontWeight = FontWeight.ExtraBold
                )
            }

            Column(
                    modifier =
                            Modifier.fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Section: General ─────────────────────────────────────
                SectionLabel(label = "General", icon = Icons.Default.Tune)

                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                                text = "Screenshot File Prefix",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF8888BB)
                        )
                        OutlinedTextField(
                                value = tempPrefix,
                                onValueChange = {
                                    tempPrefix = it
                                    viewModel.setScreenshotPrefix(it)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                placeholder = { Text("e.g. SNAP", color = Color(0xFF555588)) },
                                colors =
                                        OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Indigo60,
                                                unfocusedBorderColor = Color(0xFF2A2A50),
                                                focusedTextColor = Color(0xFFE0E0F8),
                                                unfocusedTextColor = Color(0xFFE0E0F8),
                                                cursorColor = Indigo60
                                        ),
                                shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // ── Section: Recording ───────────────────────────────────
                Spacer(modifier = Modifier.height(4.dp))
                SectionLabel(label = "Screen Recording", icon = Icons.Default.Videocam)

                SettingsCard {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                    modifier =
                                            Modifier.size(40.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Cyan50.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                        Icons.Default.Mic,
                                        null,
                                        tint = Cyan50,
                                        modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                        "Capture Audio",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFD0D0EE)
                                )
                                Text(
                                        "Include microphone",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF6666AA)
                                )
                            }
                        }
                        Switch(
                                checked = audioEnabled,
                                onCheckedChange = { viewModel.setAudioEnabled(it) },
                                colors =
                                        SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Indigo60,
                                                uncheckedThumbColor = Color(0xFF888888),
                                                uncheckedTrackColor = Surface3
                                        )
                        )
                    }
                }

                // ── Section: Recording Quality ───────────────────────────
                Spacer(modifier = Modifier.height(4.dp))
                SectionLabel(label = "Recording Quality", icon = Icons.Default.Hd)

                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                                "Select output resolution",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6666AA)
                        )
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("SD" to "480p", "HD" to "720p", "FHD" to "1080p").forEach {
                                    (quality, label) ->
                                val selected = recordQuality == quality
                                Box(
                                        modifier =
                                                Modifier.weight(1f)
                                                        .height(56.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(
                                                                if (selected)
                                                                        Brush.linearGradient(
                                                                                listOf(
                                                                                        Indigo50,
                                                                                        Violet60
                                                                                )
                                                                        )
                                                                else
                                                                        Brush.linearGradient(
                                                                                listOf(
                                                                                        Surface2,
                                                                                        Surface3
                                                                                )
                                                                        )
                                                        )
                                                        .border(
                                                                1.dp,
                                                                if (selected) Color.Transparent
                                                                else Color(0xFF2A2A50),
                                                                RoundedCornerShape(12.dp)
                                                        )
                                                        .let { mod ->
                                                            if (selected) mod
                                                            else
                                                                    mod.clickable {
                                                                        viewModel.setRecordQuality(
                                                                                quality
                                                                        )
                                                                    }
                                                        },
                                        contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                                text = quality,
                                                style = MaterialTheme.typography.labelLarge,
                                                color =
                                                        if (selected) Color.White
                                                        else Color(0xFF9999CC),
                                                fontWeight =
                                                        if (selected) FontWeight.Bold
                                                        else FontWeight.Normal
                                        )
                                        Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color =
                                                        if (selected) Color.White.copy(0.7f)
                                                        else Color(0xFF555588)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // ── Section: Tool Features ────────────────────────────────
                Spacer(modifier = Modifier.height(4.dp))
                SectionLabel(label = "Powerful Tools", icon = Icons.Default.Widgets)

                SettingsCard {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                    modifier =
                                            Modifier.size(40.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Indigo50.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                        Icons.Default.Layers,
                                        null,
                                        tint = Indigo50,
                                        modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                        "Floating Snap Bubble",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFD0D0EE)
                                )
                                Text(
                                        "Overlay button for quick actions",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF6666AA)
                                )
                            }
                        }

                        val overlayEnabled by viewModel.overlayEnabled.collectAsState()
                        Switch(
                                checked = overlayEnabled,
                                onCheckedChange = { enabled ->
                                    val overlayIntent =
                                            Intent(
                                                    context,
                                                    com.snaptool.service.OverlayService::class.java
                                            )
                                    if (enabled) {
                                        if (Settings.canDrawOverlays(context)) {
                                            viewModel.setOverlayEnabled(true)
                                            context.startService(overlayIntent)
                                        } else {
                                            val intent =
                                                    Intent(
                                                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                                    Uri.parse(
                                                                            "package:${context.packageName}"
                                                                    )
                                                            )
                                                            .apply {
                                                                addFlags(
                                                                        Intent.FLAG_ACTIVITY_NEW_TASK
                                                                )
                                                            }
                                            context.startActivity(intent)
                                        }
                                    } else {
                                        viewModel.setOverlayEnabled(false)
                                        context.stopService(overlayIntent)
                                    }
                                },
                                colors =
                                        SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Cyan50,
                                                uncheckedThumbColor = Color(0xFF888888),
                                                uncheckedTrackColor = Surface3
                                        )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Footer ───────────────────────────────────────────────
                Text(
                        text = "Snapit v1.0.0",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF44446A)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(label: String, icon: ImageVector) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(icon, null, tint = Indigo70, modifier = Modifier.size(16.dp))
        Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Indigo70,
                fontWeight = FontWeight.Bold,
                letterSpacing =
                        androidx.compose.ui.unit.TextUnit(
                                1.5f,
                                androidx.compose.ui.unit.TextUnitType.Sp
                        )
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Surface2),
            border = BorderStroke(1.dp, Color(0xFF252550))
    ) { Column(modifier = Modifier.padding(20.dp), content = content) }
}
