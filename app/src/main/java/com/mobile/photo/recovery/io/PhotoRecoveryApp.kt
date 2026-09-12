package com.mobile.photo.recovery.io

import android.app.Application
import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.appadskit.AdPlacement
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.mobile.photo.recovery.io.ads.AdMobConfig
import com.mobile.photo.recovery.io.ads.AppOpenAdManager
import com.mobile.photo.recovery.io.ads.InterstitialAdHelper
import com.mobile.photo.recovery.io.ads.NativeAds
import com.mobile.photo.recovery.io.ads.PhotoAds
import com.mobile.photo.recovery.io.ads.RewardAdHelper
import com.mobile.photo.recovery.io.util.LocaleHelper

class PhotoRecoveryApp : Application(), ImageLoaderFactory {
    private lateinit var appOpenAds: AppOpenAdManager

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.applyStoredLocale(base))
    }

    override fun onCreate() {
        super.onCreate()
        PhotoAds.bootstrap(this)
        appOpenAds = AppOpenAdManager(this)
        appOpenAds.register()
        if (AdMobConfig.useTestAds) {
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                    .build(),
            )
            Log.d(TAG, "AdMob debug: Google test ads")
        }
        MobileAds.initialize(this) { status ->
            Log.d(TAG, "AdMob ready: ${status.adapterStatusMap.size} adapters")
            RewardAdHelper.preload(this)
            InterstitialAdHelper.preload(this)
            NativeAds.preloadCache(this)
            appOpenAds.preload(this, AdPlacement.OPEN_SPLASH)
        }
    }

    // Registers VideoFrameDecoder so every AsyncImage in the app (Photo Recovery, Quick Swipe
    // Clean, etc.) can render a real video-frame thumbnail for video MediaStore items instead of
    // failing to decode them — Coil's built-in decoders only handle static image formats.
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()

    companion object {
        private const val TAG = "PhotoRecoveryApp"
    }
}
