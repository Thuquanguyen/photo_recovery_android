package com.mobile.photo.recovery.io.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.appadskit.AdFrequencyStore
import com.appadskit.AdPlacement
import com.appadskit.AdsConfig
import com.appadskit.AppAdManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.mobile.photo.recovery.io.data.Prefs

/**
 * Full-screen interstitial — same inventory rule as [NativeAds]:
 *
 * - Keep the unused cached ad while a replacement loads.
 * - No-fill / load fail never wipes a usable cache.
 * - Only drop the old unused ad when a new fill arrives.
 * - Never load / replace / destroy while an interstitial is on screen.
 * - Load the next ad only after dismiss / fail-to-show.
 */
object InterstitialAdHelper {
    private const val TAG = "InterstitialAd"
    private const val AD_MAX_AGE_MS = 55L * 60L * 1000L

    private val lock = Any()
    private var cached: InterstitialAd? = null
    private var cachedUnitId: String = ""
    private var loadedAtMs = 0L
    private var loadInFlight = false
    private var showing: InterstitialAd? = null
    private var loadAfterShow = false

    fun preload(context: Context, placement: String = AdPlacement.INTER_BEFORE_RENT) {
        if (Prefs.get(context).isVip) return
        val unitId = AdsConfig.interstitial(placement).adUnitId
        val appCtx = context.applicationContext
        var alreadyReady = false
        synchronized(lock) {
            if (showing != null) {
                Log.i(TAG, "preload skipped — interstitial is showing")
                loadAfterShow = true
                return
            }
            alreadyReady = hasReady(unitId)
            if (loadInFlight) return
            loadInFlight = true
        }
        Log.i(TAG, "load unit=${unitId.takeLast(8)} keepOld=$alreadyReady")
        InterstitialAd.load(
            appCtx,
            unitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "load fail (keep cache, keep showing): ${error.message}")
                    synchronized(lock) { loadInFlight = false }
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    synchronized(lock) {
                        loadInFlight = false
                        if (showing != null && showing === ad) return
                        cached = ad
                        cachedUnitId = unitId
                        loadedAtMs = System.currentTimeMillis()
                    }
                    Log.i(TAG, "loaded — replaced unused cache")
                }
            },
        )
    }

    /** @return true if an interstitial was shown (caller waits for [onFinished]). */
    fun showIfReady(
        activity: Activity,
        placement: String = AdPlacement.INTER_BEFORE_RENT,
        onFinished: () -> Unit,
    ): Boolean {
        when (AppAdManager.interstitialDecision(placement)) {
            AppAdManager.InterstitialDecision.Skip -> {
                onFinished()
                return false
            }
            is AppAdManager.InterstitialDecision.Show -> Unit
        }
        if (Prefs.get(activity).isVip) {
            onFinished()
            return false
        }
        if (FullScreenAdGate.isBusy()) {
            onFinished()
            return false
        }
        if (FullScreenAdGate.consumeSkipInterstitial()) {
            Log.d(TAG, "skip interstitial (after reward)")
            onFinished()
            return false
        }
        val unitId = AdsConfig.interstitial(placement).adUnitId
        synchronized(lock) {
            if (showing != null) {
                onFinished()
                return false
            }
        }
        if (!hasReady(unitId)) {
            preload(activity, placement)
            onFinished()
            return false
        }
        if (!FullScreenAdGate.acquire()) {
            onFinished()
            return false
        }
        val ad = synchronized(lock) {
            if (showing != null) {
                FullScreenAdGate.release()
                null
            } else {
                val ready = cached
                showing = ready
                if (cached === ready) {
                    cached = null
                    cachedUnitId = ""
                    loadedAtMs = 0L
                }
                ready
            }
        } ?: run {
            FullScreenAdGate.release()
            preload(activity, placement)
            onFinished()
            return false
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                finishPresent(activity, placement, onFinished)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                FullScreenAdGate.release()
                synchronized(lock) { showing = null }
                preload(activity, placement)
                onFinished()
            }
        }
        ad.show(activity)
        return true
    }

    private fun finishPresent(activity: Activity, placement: String, onFinished: () -> Unit) {
        val shouldPreload: Boolean
        synchronized(lock) {
            showing = null
            shouldPreload = loadAfterShow || cached == null
            loadAfterShow = false
        }
        FullScreenAdGate.release()
        if (shouldPreload) preload(activity, placement)
        AdFrequencyStore.markInterstitialShown(placement)
        onFinished()
    }

    private fun hasReady(unitId: String): Boolean {
        val ad = cached ?: return false
        if (ad === showing) return false
        if (cachedUnitId.isNotEmpty() && cachedUnitId != unitId) return false
        return loadedAtMs > 0L && System.currentTimeMillis() - loadedAtMs < AD_MAX_AGE_MS
    }
}
