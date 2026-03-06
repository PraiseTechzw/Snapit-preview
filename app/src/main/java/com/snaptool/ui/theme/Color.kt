package com.snaptool.ui.theme

import androidx.compose.ui.graphics.Color

// ── Brand palette ──────────────────────────────────────────────────────────────
val Indigo10   = Color(0xFF0D0D1A)
val Indigo20   = Color(0xFF13132B)
val Indigo30   = Color(0xFF1C1C3D)
val Indigo40   = Color(0xFF252558)
val Indigo50   = Color(0xFF3535A0)   // primary accent
val Indigo60   = Color(0xFF5757D0)
val Indigo70   = Color(0xFF7B7BE8)
val Indigo80   = Color(0xFFA8A8F0)
val Indigo90   = Color(0xFFD4D4F8)
val Indigo95   = Color(0xFFECECFD)

val Violet50   = Color(0xFF7C3AED)
val Violet60   = Color(0xFF9B5EFF)
val Violet70   = Color(0xFFB78AFF)

val Cyan50     = Color(0xFF06B6D4)
val Cyan60     = Color(0xFF22D3EE)
val Cyan70     = Color(0xFF67E8F9)

val ErrorRed   = Color(0xFFFF4545)
val RecordRed  = Color(0xFFFF3B3B)
val SuccessGreen = Color(0xFF22C55E)

// ── Surface shades ─────────────────────────────────────────────────────────────
val Surface0   = Color(0xFF0A0A18)
val Surface1   = Color(0xFF111127)
val Surface2   = Color(0xFF17173A)
val Surface3   = Color(0xFF1E1E4A)
val SurfaceGlass = Color(0x1AFFFFFF)   // white 10 % – glassmorphism tint
val OutlineGlass = Color(0x33FFFFFF)   // white 20 % – glass border

// ── Legacy names kept for Theme.kt ────────────────────────────────────────────
val Purple80     = Indigo80
val PurpleGrey80 = Indigo70
val Pink80       = Violet70

val Purple40     = Indigo50
val PurpleGrey40 = Indigo40
val Pink40       = Violet50

@Suppress("unused")
val RecordingRed = RecordRed
