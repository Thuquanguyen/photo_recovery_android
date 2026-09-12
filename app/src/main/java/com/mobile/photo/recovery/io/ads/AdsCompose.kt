package com.mobile.photo.recovery.io.ads

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.appadskit.AppAdManager
import com.appadskit.AdsKit

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
        modifier = modifier.fillMaxWidth(),
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
        modifier = modifier.fillMaxWidth(),
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
