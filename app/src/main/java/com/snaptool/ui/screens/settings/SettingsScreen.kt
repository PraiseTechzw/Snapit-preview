package com.snaptool.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.snaptool.ui.theme.*
import com.snaptool.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val screenshotPrefix by viewModel.screenshotPrefix.collectAsState()
    val audioEnabled by viewModel.audioEnabled.collectAsState()
    val recordQuality by viewModel.recordQuality.collectAsState()
    val overlayEnabled by viewModel.overlayEnabled.collectAsState()

    var tempPrefix by remember(screenshotPrefix) { mutableStateOf(screenshotPrefix) }

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
                Icon(Icons.Default.ArrowBack, "Back", tint = Indigo80)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // ── Scrollable content ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── General ─────────────────────────────────────────────────────
            SectionLabel("GENERAL")

            SettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Screenshot File Prefix",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD0D0EE),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Prefix added to screenshot filenames",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6666AA)
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
                        placeholder = { Text("e.g. SNAP_", color = Color(0xFF555588)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Indigo60,
                            unfocusedBorderColor = Color(0xFF252550),
                            focusedTextColor = Color(0xFFE0E0F8),
                            unfocusedTextColor = Color(0xFFE0E0F8),
                            cursorColor = Indigo60
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Screen Recording ─────────────────────────────────────────────
            SectionLabel("SCREEN RECORDING")

            // Audio toggle
            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Mic,
                    iconTint = Cyan50,
                    title = "Record Audio",
                    subtitle = "Include microphone in recordings"
                ) {
                    Switch(
                        checked = audioEnabled,
                        onCheckedChange = { viewModel.setAudioEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Indigo60,
                            uncheckedThumbColor = Color(0xFF888888),
                            uncheckedTrackColor = Surface3
                        )
                    )
                }
            }

            // Quality selector
            SettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Recording Quality",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD0D0EE),
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("SD" to "480p", "HD" to "720p", "FHD" to "1080p").forEach { (quality, label) ->
                            val selected = recordQuality == quality
                            OutlinedButton(
                                onClick = { viewModel.setRecordQuality(quality) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selected) Indigo50 else Color.Transparent,
                                    contentColor = if (selected) Color.White else Color(0xFF9999CC)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) Indigo50 else Color(0xFF252550)
                                )
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(quality, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                                    Text(label, style = MaterialTheme.typography.labelSmall, color = if (selected) Color.White.copy(0.7f) else Color(0xFF555588))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Tools ────────────────────────────────────────────────────────
            SectionLabel("TOOLS")

            SettingsCard {
                SettingsRow(
                    icon = Icons.Default.Layers,
                    iconTint = Indigo60,
                    title = "Floating Snap Bubble",
                    subtitle = "Overlay button for capturing anywhere"
                ) {
                    Switch(
                        checked = overlayEnabled,
                        onCheckedChange = { enabled ->
                            val overlayIntent = Intent(context, com.snaptool.service.OverlayService::class.java)
                            if (enabled) {
                                if (Settings.canDrawOverlays(context)) {
                                    viewModel.setOverlayEnabled(true)
                                    context.startService(overlayIntent)
                                } else {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                    context.startActivity(intent)
                                }
                            } else {
                                viewModel.setOverlayEnabled(false)
                                context.stopService(overlayIntent)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Cyan50,
                            uncheckedThumbColor = Color(0xFF888888),
                            uncheckedTrackColor = Surface3
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFF555588),
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface2),
        border = BorderStroke(1.dp, Color(0xFF252550))
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFD0D0EE), fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6666AA))
            }
        }
        trailing()
    }
}
