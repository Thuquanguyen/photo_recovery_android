package com.mobile.photo.recovery.io.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Light palette — taken from the mockups' own Tailwind token block, which every
// light screen (Language, Onboarding, Photo Recovery, Quick Swipe Clean,
// Duplicate Cleaner, Screenshot Cleaner, Settings) is built on.
// ---------------------------------------------------------------------------
val Primary = Color(0xFF6B38D4)
val PrimaryContainer = Color(0xFF8455EF)
val PrimaryFixed = Color(0xFFE9DDFF)
val PrimaryFixedDim = Color(0xFFD0BCFF)
val OnPrimaryFixed = Color(0xFF23005C)

val Secondary = Color(0xFF006E1C)
val SecondaryContainer = Color(0xFF91F78E)
val OnSecondaryContainer = Color(0xFF00731E)
val SuccessGreen = Color(0xFF16A34A)

val Tertiary = Color(0xFF755700)
val TertiaryContainer = Color(0xFF946F00)
val TertiaryFixed = Color(0xFFFFDF9E)
val TertiaryFixedDim = Color(0xFFFABD00)
val OnTertiaryFixed = Color(0xFF261A00)

val DangerRed = Color(0xFFBA1A1A)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF93000A)

val Surface = Color(0xFFFAF8FF)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFF2F3FF)
val SurfaceContainer = Color(0xFFEAEDFF)
val SurfaceContainerHigh = Color(0xFFE2E7FF)
val SurfaceContainerHighest = Color(0xFFDAE2FF)
val SurfaceDim = Color(0xFFCED9FF)

val OnSurface = Color(0xFF0D1A38)
val OnSurfaceVariant = Color(0xFF494454)
val Outline = Color(0xFF7B7486)
val OutlineVariant = Color(0xFFCBC3D7)
val InverseSurface = Color(0xFF23304E)
val InverseOnSurface = Color(0xFFEEF0FF)

// Legacy aliases kept so existing call sites keep compiling.
val NeutralDark = OnSurface
val LavenderBackground = SurfaceContainer
val CardSurface = SurfaceContainerLowest

// ---------------------------------------------------------------------------
// Dark glassmorphic palette — used ONLY by Splash, Home Hub and Premium PRO,
// which their own mockups render on a deep purple gradient.
// ---------------------------------------------------------------------------
// ~70% luminance of the mockup's literal stops (#180b2a/#290f4e/#3d126b/#56147d/#7b188c) — the
// literal values read too bright/washed out once rendered full-screen on device, per user feedback.
val DarkGradientStops = listOf(
    Color(0xFF120821),
    Color(0xFF1D0A37),
    Color(0xFF2B0D4B),
    Color(0xFF3C0E58),
    Color(0xFF561162)
)
val GlassFill = Color(0x1AFFFFFF)
val GlassStroke = Color(0x26FFFFFF)
val AccentCyan = Color(0xFF67E8F9)
val AccentAmber = Color(0xFFFBBF24)
val AccentEmerald = Color(0xFF34D399)
val AccentPink = Color(0xFFEC4899)
val AccentViolet = Color(0xFFC4B5FD)
