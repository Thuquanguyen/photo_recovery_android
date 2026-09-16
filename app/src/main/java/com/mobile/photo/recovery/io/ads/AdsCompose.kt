package com.mobile.photo.recovery.io.ads

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.appadskit.AppAdManager
import com.appadskit.AdsKit

/** Matches [NativeAds.bind]'s own minimumHeight-per-layout so Compose reserves the slot's
 * space up front — without this, the AndroidView collapses to 0 height until the ad view
 * system finishes its own async layout pass, letting the next sibling render over it. */
private fun NativeAdLayout.minHeightDp(): androidx.compose.ui.unit.Dp = when (this) {
    NativeAdLayout.Compact, NativeAdLayout.Side -> 0.dp
    NativeAdLayout.Medium -> 168.dp
    NativeAdLayout.Default -> 280.dp
}

/** Standard AdSize.BANNER height used by [BannerAds.bind]. */
private val BANNER_MIN_HEIGHT = 50.dp

@Composable
fun rememberHostActivity(): Activity? {
    return LocalActivityResultRegistryOwner.current as? Activity
}

@Composable
fun NativeAdSlot(
    placement: String,
    layout: NativeAdLayout = NativeAdLayout.Medium,
    modifier: Modifier = Modifier,
) {
    val visible = remember(placement) {
        AppAdManager.shouldShowNative(placement) != null && !AdsKit.isVip
    }
    if (!visible) return
    AndroidView(
        modifier = modifier.fillMaxWidth().heightIn(min = layout.minHeightDp()),
        factory = { ctx ->
            FrameLayout(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                NativeAds.bind(this, ctx, layout, placement)
            }
        },
    )
    DisposableEffect(placement) {
        onDispose {
            NativeAds.clear()
        }
    }
}

@Composable
fun BannerAdSlot(
    placement: String,
    modifier: Modifier = Modifier,
) {
    val visible = remember(placement) {
        AppAdManager.shouldShowBanner(placement) != null && !AdsKit.isVip
    }
    if (!visible) return
    AndroidView(
        modifier = modifier.fillMaxWidth().heightIn(min = BANNER_MIN_HEIGHT),
        factory = { ctx ->
            FrameLayout(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                BannerAds.bind(this, ctx, placement)
            }
        },
    )
}

fun Activity.showInterstitial(placement: String, onFinished: () -> Unit) {
    InterstitialAdHelper.showIfReady(this, placement, onFinished)
}

@Composable
fun InterstitialBackHandler(
    placement: String,
    enabled: Boolean = true,
    onProceed: () -> Unit,
) {
    val activity = rememberHostActivity()
    BackHandler(enabled) {
        if (activity != null) {
            InterstitialAdHelper.showIfReady(activity, placement, onProceed)
        } else {
            onProceed()
        }
    }
}
