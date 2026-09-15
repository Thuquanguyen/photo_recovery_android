package com.mobile.photo.recovery.io.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appadskit.AdPlacement
import com.appadskit.AdsConfig
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.ads.AdsSplashGate
import com.mobile.photo.recovery.io.ads.AppOpenAdManager
import com.mobile.photo.recovery.io.ads.InterstitialAdHelper
import com.mobile.photo.recovery.io.ads.NativeAds
import com.mobile.photo.recovery.io.ads.rememberHostActivity
import com.mobile.photo.recovery.io.data.Prefs
import com.mobile.photo.recovery.io.ui.theme.AccentCyan
import com.mobile.photo.recovery.io.ui.theme.AccentEmerald
import com.mobile.photo.recovery.io.ui.theme.AccentViolet
import kotlinx.coroutines.delay
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Branded splash — the dark glassmorphic purple gradient of its own mockup (splash_screen/code.html).
 * The loading runner is purely cosmetic (spec 4.1); the real work is preloading the App Open /
 * interstitial / native ad caches, waiting [AdsConfig.splashFirstOpenDelaySeconds], showing the
 * open_splash App Open ad, then reading the onboarding-completed flag to decide where to go.
 */
private val SplashBrandViolet = Color(0xFF8B5CF6)

@Composable
fun SplashScreen(onFinished: (onboardingCompleted: Boolean) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = rememberHostActivity()
    val versionName = remember(context) {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        } catch (_: Exception) {
            ""
        }
    }
    val delaySec = AdsConfig.splashFirstOpenDelaySeconds.coerceIn(0, 120)
    val splashDurationMs = (delaySec * 1000).coerceAtLeast(1_200)

    DisposableEffect(Unit) {
        AdsSplashGate.active = true
        onDispose { AdsSplashGate.active = false }
    }

    LaunchedEffect(Unit) {
        AppOpenAdManager.get()?.preload(context, AdPlacement.OPEN_SPLASH)
        InterstitialAdHelper.preload(context)
        NativeAds.preloadCache(context, AdPlacement.NATIVE_LANGUAGE)
        delay(splashDurationMs.toLong())
        val host = activity
        if (host != null) {
            suspendCoroutine { cont ->
                AppOpenAdManager.get()?.showSplash(host) { cont.resume(Unit) } ?: cont.resume(Unit)
            }
        }
        AdsSplashGate.active = false
        onFinished(Prefs.get(context).onboardingCompleted)
    }

    // Indeterminate shimmer runner (mockup's `animate-shimmer`): a translucent-to-bright segment
    // sliding left-to-right on loop, not a real progress fill — the real work here is near-instant.
    val shimmerTransition = rememberInfiniteTransition(label = "splashShimmer")
    val shimmerX by shimmerTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerX"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF140B24), Color(0xFF240C4A), Color(0xFF0E061A)))
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ambient glow blooms — soft/diffuse per the mockup's blur-[80..100px], not hard-edged circles.
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset((-80).dp, (-80).dp)
                .size(300.dp)
                .blur(90.dp, BlurredEdgeTreatment.Unbounded)
                .background(SplashBrandViolet.copy(alpha = 0.18f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(280.dp)
                .blur(80.dp, BlurredEdgeTreatment.Unbounded)
                .background(SplashBrandViolet.copy(alpha = 0.15f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(80.dp, 80.dp)
                .size(300.dp)
                .blur(100.dp, BlurredEdgeTreatment.Unbounded)
                .background(Color(0xFFD946EF).copy(alpha = 0.12f), CircleShape)
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(1.dp))

            // Brand identity block.
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.35f), SplashBrandViolet.copy(alpha = 0.2f), Color.Transparent)
                            )
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // R.mipmap.ic_launcher is an <adaptive-icon> XML — painterResource() can't
                    // load that (only VectorDrawables/raster assets), hence ic_launcher_foreground
                    // (a plain raster PNG) here instead. That foreground layer bakes in Android's
                    // adaptive-icon safe-zone padding (~66% visible), so scale it up to crop that
                    // margin away and fill the squircle edge-to-edge like the mockup's logo image.
                    Image(
                        painter = painterResource(R.mipmap.ic_launcher_foreground),
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp))
                            .scale(1.55f)
                    )
                    // Subtle lens-reflection accent (top-right of the squircle).
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(14.dp)
                            .background(Color.White.copy(alpha = 0.4f), CircleShape)
                    )
                }
                Spacer(Modifier.height(28.dp))
                Row {
                    Text(
                        stringResource(R.string.splash_title_part1),
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White
                    )
                    Text(
                        stringResource(R.string.splash_title_part2),
                        style = MaterialTheme.typography.displaySmall.copy(
                            brush = Brush.linearGradient(
                                listOf(AccentViolet.copy(alpha = 0.9f), Color.White, Color(0xFFE9D5FF))
                            )
                        )
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.splash_tagline_scan),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 2.4.sp),
                        color = AccentViolet.copy(alpha = 0.85f)
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(3.dp)
                            .background(AccentViolet, CircleShape)
                    )
                    Text(
                        stringResource(R.string.splash_tagline_restore),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 2.4.sp),
                        color = AccentViolet.copy(alpha = 0.85f)
                    )
                }
            }

            // Loading & engine status block.
            Column(
                modifier = Modifier.fillMaxWidth(0.82f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indeterminate shimmer runner track.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E1065).copy(alpha = 0.7f))
                        .border(1.dp, SplashBrandViolet.copy(alpha = 0.2f), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(6.dp)
                            .align(Alignment.CenterStart)
                            .graphicsLayerTranslateFraction(shimmerX)
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(Color.Transparent, AccentCyan, SplashBrandViolet)))
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(SplashBrandViolet, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.splash_initializing),
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentViolet.copy(alpha = 0.6f)
                    )
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = null,
                        tint = AccentEmerald.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "v$versionName",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.45f)
                    )
                    Text(
                        "  •  ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.2f)
                    )
                    Text(
                        stringResource(R.string.splash_privacy),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/** Slides this element horizontally by [fraction] * its own width (-1..1), for the shimmer runner. */
private fun Modifier.graphicsLayerTranslateFraction(fraction: Float): Modifier = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.place((fraction * placeable.width).toInt(), 0)
    }
}
