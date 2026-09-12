package com.mobile.photo.recovery.io.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.appadskit.AdFrequencyStore
import com.appadskit.AdPlacement
import com.appadskit.AdsConfig
import com.appadskit.AppAdManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.mobile.photo.recovery.io.data.Prefs

/**
 * Rewarded — same inventory rule as [NativeAds]:
 * keep unused cache while a replacement loads; no-fill never wipes a usable ad.
 */
object RewardAdHelper {
    private const val TAG = "RewardAdHelper"
    private const val AD_MAX_AGE_MS = 55L * 60L * 1000L

    private val lock = Any()
    private var cached: RewardedAd? = null
    private var cachedUnitId: String = ""
    private var loadedAtMs = 0L
    private var loadInFlight = false
    private var showing: RewardedAd? = null
    private val waiters = mutableListOf<(Boolean) -> Unit>()

    fun preload(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        if (Prefs.get(context).isVip) {
            onComplete?.invoke(true)
            return
        }
        val unitId = AdsConfig.rewarded(AdPlacement.REWARD_CONTINUE).adUnitId
        val appCtx = context.applicationContext
        var alreadyReady = false
        synchronized(lock) {
            if (showing != null) {
                alreadyReady = hasReady()
                if (alreadyReady) onComplete?.invoke(true)
                else if (onComplete != null) waiters.add(onComplete)
                return
            }
            alreadyReady = hasReady()
            if (alreadyReady) onComplete?.invoke(true)
            if (loadInFlight) {
                if (!alreadyReady && onComplete != null) waiters.add(onComplete)
                return
            }
            loadInFlight = true
        }
        Log.i(TAG, "load keepOld=$alreadyReady")
        RewardedAd.load(
            appCtx,
            unitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Rewarded load fail (keep cache): ${error.message}")
                    val stillReady = synchronized(lock) {
                        loadInFlight = false
                        val ok = hasReady()
                        drainWaiters(ok)
                        ok
                    }
                    if (!alreadyReady) onComplete?.invoke(stillReady)
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    synchronized(lock) {
                        loadInFlight = false
                        if (showing != null && showing === ad) {
                            drainWaiters(hasReady())
                            return
                        }
                        cached = ad
                        cachedUnitId = unitId
                        loadedAtMs = System.currentTimeMillis()
                        drainWaiters(true)
                    }
                    Log.d(TAG, "Rewarded loaded — replaced unused cache")
                    if (!alreadyReady) onComplete?.invoke(true)
                }
            },
        )
    }

    fun show(
        context: Context,
        placement: String = AdPlacement.REWARD_CONTINUE,
        onRewarded: () -> Unit,
        onFailed: ((String) -> Unit)? = null,
    ) {
        val activity = context.findActivity()
        if (activity == null) {
            onFailed?.invoke("no activity")
            return
        }
        if (AppAdManager.shouldShowRewarded(placement) == null) {
            if (Prefs.get(context).isVip) onRewarded()
            else onFailed?.invoke("rewarded disabled")
            return
        }
        if (Prefs.get(context).isVip) {
            onRewarded()
            return
        }
        if (!FullScreenAdGate.beginRewardFlow()) {
            onFailed?.invoke("another ad is showing")
            return
        }
        val ready = synchronized(lock) { if (hasReady()) cached else null }
        if (ready != null) {
            present(activity, ready, onRewarded, onFailed)
            return
        }
        preload(context) { ok ->
            val ad = synchronized(lock) { cached }
            if (!ok || ad == null) {
                FullScreenAdGate.endRewardFlow()
                onFailed?.invoke("ad failed to load")
                return@preload
            }
            present(activity, ad, onRewarded, onFailed)
        }
    }

    private fun present(
        activity: Activity,
        ad: RewardedAd,
        onRewarded: () -> Unit,
        onFailed: ((String) -> Unit)?,
    ) {
        if (!FullScreenAdGate.acquire()) {
            FullScreenAdGate.endRewardFlow()
            onFailed?.invoke("another ad is showing")
            return
        }
        synchronized(lock) {
            showing = ad
            if (cached === ad) {
                cached = null
                cachedUnitId = ""
                loadedAtMs = 0L
            }
        }
        var earned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                synchronized(lock) { showing = null }
                FullScreenAdGate.release()
                FullScreenAdGate.markSkipNextInterstitial()
                preload(activity.applicationContext)
                if (earned) {
                    AdFrequencyStore.markRewardedShown()
                    onRewarded()
                } else {
                    onFailed?.invoke("dismissed without reward")
                }
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                synchronized(lock) { showing = null }
                FullScreenAdGate.release()
                Log.w(TAG, "Show failed: ${adError.message}")
                preload(activity.applicationContext)
                onFailed?.invoke(adError.message)
            }
        }
        ad.show(activity) {
            earned = true
            Log.d(TAG, "Reward earned")
        }
    }

    private fun hasReady(): Boolean {
        val ad = cached ?: return false
        if (ad === showing) return false
        if (cachedUnitId.isNotEmpty() &&
            cachedUnitId != AdsConfig.rewarded(AdPlacement.REWARD_CONTINUE).adUnitId
        ) {
            return false
        }
        return loadedAtMs > 0L && System.currentTimeMillis() - loadedAtMs < AD_MAX_AGE_MS
    }

    private fun drainWaiters(ok: Boolean) {
        val copy = waiters.toList()
        waiters.clear()
        copy.forEach { it(ok) }
    }
}

internal fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
