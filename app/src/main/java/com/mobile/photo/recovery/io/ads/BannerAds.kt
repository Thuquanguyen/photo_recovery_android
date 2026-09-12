package com.mobile.photo.recovery.io.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.appadskit.AdFrequencyStore
import com.appadskit.AdPlacement
import com.appadskit.AdsConfig
import com.appadskit.AppAdManager
import com.appadskit.NativePlacement
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.data.Prefs

object BannerAds {
    fun bind(
        container: FrameLayout,
        context: Context,
        placement: String = AdPlacement.BANNER_SETTINGS,
    ) {
        val cfg = AppAdManager.shouldShowBanner(placement)
        if (cfg == null || Prefs.get(context).isVip) {
            container.visibility = View.GONE
            container.removeAllViews()
            return
        }
        container.visibility = View.VISIBLE
        if (container.childCount > 0 && container.getChildAt(0) is AdView) return
        container.removeAllViews()
        val adView = AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = cfg.adUnitId
            adListener = object : AdListener() {
                override fun onAdClicked() {
                    AdFrequencyStore.recordBannerClick(AdsConfig.bannerMaxClicksPerDay)
                    if (!AdFrequencyStore.canClickBanner(AdsConfig.bannerMaxClicksPerDay)) {
                        container.visibility = View.GONE
                        container.removeAllViews()
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w("BannerAds", error.message)
                    container.visibility = View.GONE
                }
            }
        }
        container.addView(adView)
        adView.loadAd(AdRequest.Builder().build())
    }
}

enum class NativeAdLayout {
    Compact,
    Medium,
    Default,
    Side,
}

/**
 * Hybrid native inventory — same rules as CloudDesk:
 *
 * 1. Cold (no cache): skeleton → first fill → UI + shared cache.
 * 2. Later bind (has cache): show cache immediately, fetch a replacement in the
 *    background; swap UI + cache only when the new fill arrives.
 * 3. Timer refresh from JSON `isReload` / `reloadIntervalSeconds`: same as (2).
 * 4. No-fill never wipes a usable ad. Destroy the previous creative only after a new fill.
 */
object NativeAds {
    private const val TAG = "NativeAds"
    private const val AD_SOFT_MAX_AGE_MS = 55L * 60L * 1000L

    private val mainHandler = Handler(Looper.getMainLooper())

    private var cached: NativeAd? = null
    private var cachedUnitId: String = ""
    private var loadedAtMs = 0L
    private var loadInFlight = false

    private var boundContainer: FrameLayout? = null
    private var boundContext: Context? = null
    private var boundPlacement: String = AdPlacement.NATIVE_HOME
    private var boundLayout: NativeAdLayout = NativeAdLayout.Default
    private var boundUnitId: String = ""
    private var boundOnReady: (() -> Unit)? = null
    private var boundOnFailed: (() -> Unit)? = null
    private var boundWaitForNewFill = false

    private val refreshRunnable = object : Runnable {
        override fun run() {
            val container = boundContainer ?: return
            val context = boundContext ?: return
            if (!container.isAttachedToWindow || container.visibility != View.VISIBLE) return
            val cfg = AdsConfig.native(boundPlacement)
            if (!cfg.isShow || !cfg.isReload || Prefs.get(context).isVip) return
            Log.i(TAG, "timer refresh $boundPlacement — keep showing, fetch replacement")
            requestNewAd(context, cfg.adUnitId)
            scheduleRefresh(cfg)
        }
    }

    fun preloadCache(context: Context, placement: String = AdPlacement.NATIVE_LANGUAGE) {
        if (Prefs.get(context).isVip) return
        val cfg = AppAdManager.shouldShowNative(placement) ?: return
        if (cfg.adUnitId.isBlank()) return
        if (usableCached(cfg.adUnitId) != null) {
            Log.d(TAG, "preloadCache skip — already warm")
            return
        }
        Log.i(TAG, "preloadCache $placement (splash warm)")
        requestNewAd(context, cfg.adUnitId)
    }

    fun bind(
        container: FrameLayout,
        context: Context,
        layout: NativeAdLayout = NativeAdLayout.Compact,
        placement: String = AdPlacement.NATIVE_HOME,
        onReady: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null,
    ) {
        attach(container, context, layout, placement, waitForNewFill = false, onReady, onFailed)
    }

    fun reload(
        container: FrameLayout,
        context: Context,
        layout: NativeAdLayout = NativeAdLayout.Compact,
        placement: String = AdPlacement.NATIVE_HOME,
        onReady: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null,
    ) {
        attach(container, context, layout, placement, waitForNewFill = true, onReady, onFailed)
    }

    fun clear() {
        clearBinding()
    }

    private fun attach(
        container: FrameLayout,
        context: Context,
        layout: NativeAdLayout,
        placement: String,
        waitForNewFill: Boolean,
        onReady: (() -> Unit)?,
        onFailed: (() -> Unit)?,
    ) {
        val cfg = AppAdManager.shouldShowNative(placement)
        if (cfg == null || Prefs.get(context).isVip) {
            if (boundContainer === container) clearBinding()
            container.visibility = View.GONE
            container.removeAllViews()
            onFailed?.invoke()
            return
        }
        val unitId = cfg.adUnitId
        boundContainer = container
        boundContext = context.applicationContext
        boundPlacement = placement
        boundLayout = layout
        boundUnitId = unitId
        boundOnReady = onReady
        boundOnFailed = onFailed
        boundWaitForNewFill = waitForNewFill

        container.visibility = View.VISIBLE
        container.minimumHeight = when (layout) {
            NativeAdLayout.Compact, NativeAdLayout.Side -> 0
            NativeAdLayout.Medium -> (168 * context.resources.displayMetrics.density).toInt()
            NativeAdLayout.Default -> (280 * context.resources.displayMetrics.density).toInt()
        }

        val usable = usableCached(unitId)
        if (usable != null) {
            Log.i(TAG, "bind $placement — show cache, fetch new (waitNew=$waitForNewFill)")
            populate(container, context, usable, layout)
            if (!waitForNewFill) onReady?.invoke()
            requestNewAd(context, unitId)
            scheduleRefresh(cfg)
            return
        }

        Log.i(TAG, "bind $placement — no cache (inFlight=$loadInFlight)")
        showLoading(container, layout)
        if (!loadInFlight) {
            requestNewAd(context, unitId)
        }
        scheduleRefresh(cfg)
    }

    private fun clearBinding() {
        mainHandler.removeCallbacks(refreshRunnable)
        boundContainer = null
        boundContext = null
        boundUnitId = ""
        boundOnReady = null
        boundOnFailed = null
        boundWaitForNewFill = false
    }

    private fun reloadIntervalMs(cfg: NativePlacement): Long =
        if (cfg.isReload) cfg.reloadIntervalSeconds.coerceIn(15, 600) * 1000L
        else Long.MAX_VALUE / 4

    private fun scheduleRefresh(cfg: NativePlacement) {
        mainHandler.removeCallbacks(refreshRunnable)
        if (!cfg.isReload) return
        mainHandler.postDelayed(refreshRunnable, reloadIntervalMs(cfg))
    }

    private fun usableCached(unitId: String): NativeAd? {
        val ad = cached ?: return null
        if (cachedUnitId != unitId) return null
        if (System.currentTimeMillis() - loadedAtMs >= AD_SOFT_MAX_AGE_MS) return null
        return ad
    }

    private fun requestNewAd(context: Context, unitId: String) {
        if (loadInFlight) {
            Log.d(TAG, "requestNewAd skipped — load in flight")
            return
        }
        loadInFlight = true
        val appCtx = context.applicationContext
        AdLoader.Builder(appCtx, unitId)
            .forNativeAd { nativeAd ->
                val previous = cached
                cached = nativeAd
                cachedUnitId = unitId
                loadedAtMs = System.currentTimeMillis()
                loadInFlight = false
                val waitNew = boundWaitForNewFill
                boundWaitForNewFill = false
                val target = boundContainer
                val ready = boundOnReady
                if (
                    target != null &&
                    target.isAttachedToWindow &&
                    target.visibility == View.VISIBLE &&
                    boundUnitId == unitId
                ) {
                    Log.i(TAG, "new fill → swap UI + cache ($boundPlacement)")
                    populate(target, target.context, nativeAd, boundLayout)
                    ready?.invoke()
                } else {
                    Log.i(TAG, "new fill → cache only (no visible slot)")
                    if (waitNew) boundOnFailed?.invoke()
                }
                if (previous != null && previous !== nativeAd) {
                    try {
                        previous.destroy()
                    } catch (_: Exception) {
                    }
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "no-fill/fail ($boundPlacement): ${error.message}")
                    loadInFlight = false
                    val waitNew = boundWaitForNewFill
                    boundWaitForNewFill = false
                    val target = boundContainer
                    val fallback = usableCached(unitId)
                    if (target != null && target.isAttachedToWindow && target.visibility == View.VISIBLE) {
                        if (fallback != null) {
                            populate(target, target.context, fallback, boundLayout)
                        } else {
                            val onlySkeletonOrEmpty = target.childCount == 0 ||
                                target.findViewWithTag<View>("native_loading") != null
                            if (onlySkeletonOrEmpty) {
                                target.removeAllViews()
                                target.visibility = View.GONE
                                boundOnFailed?.invoke()
                                return
                            }
                        }
                    }
                    if (waitNew) boundOnFailed?.invoke()
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    private fun slotParams(): FrameLayout.LayoutParams =
        FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ).apply { gravity = Gravity.TOP }

    private fun showLoading(container: FrameLayout, layout: NativeAdLayout) {
        container.removeAllViews()
        val layoutRes = when (layout) {
            NativeAdLayout.Compact, NativeAdLayout.Side -> R.layout.view_native_ad_loading_compact
            NativeAdLayout.Medium -> R.layout.view_native_ad_loading_medium
            NativeAdLayout.Default -> R.layout.view_native_ad_loading
        }
        val loading = LayoutInflater.from(container.context).inflate(layoutRes, container, false)
        loading.tag = "native_loading"
        container.addView(loading, slotParams())
    }

    private fun populate(
        container: FrameLayout,
        context: Context,
        nativeAd: NativeAd,
        layout: NativeAdLayout,
    ) {
        container.removeAllViews()
        val layoutRes = when (layout) {
            NativeAdLayout.Compact -> R.layout.view_native_ad
            NativeAdLayout.Medium -> R.layout.view_native_ad_medium
            NativeAdLayout.Default -> R.layout.view_native_ad_feed
            NativeAdLayout.Side -> R.layout.view_native_ad_side
        }
        val adView = LayoutInflater.from(context)
            .inflate(layoutRes, container, false) as NativeAdView
        val headline = adView.findViewById<TextView>(R.id.adHeadline)
        val body = adView.findViewById<TextView>(R.id.adBody)
        val cta = adView.findViewById<Button>(R.id.adCallToAction)
        val icon = adView.findViewById<ImageView>(R.id.adIcon)
        val media = adView.findViewById<MediaView>(R.id.adMedia)
        headline.text = nativeAd.headline
        body.text = nativeAd.body
        body.visibility = if (nativeAd.body.isNullOrBlank()) View.GONE else View.VISIBLE
        cta.text = nativeAd.callToAction
        cta.visibility = if (nativeAd.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE
        if (layout == NativeAdLayout.Side) {
            val hasMedia = nativeAd.mediaContent != null
            if (hasMedia) {
                media.visibility = View.VISIBLE
                media.mediaContent = nativeAd.mediaContent
                icon.visibility = View.GONE
                icon.setImageDrawable(null)
                adView.mediaView = media
                adView.iconView = null
            } else {
                media.visibility = View.GONE
                media.mediaContent = null
                icon.setImageDrawable(nativeAd.icon?.drawable)
                icon.visibility = if (nativeAd.icon == null) View.GONE else View.VISIBLE
                adView.mediaView = null
                adView.iconView = icon
            }
        } else if (layout == NativeAdLayout.Compact) {
            icon.setImageDrawable(nativeAd.icon?.drawable)
            icon.visibility = if (nativeAd.icon == null) View.GONE else View.VISIBLE
            adView.iconView = icon
            media.visibility = View.GONE
            adView.mediaView = media
        } else {
            icon.setImageDrawable(nativeAd.icon?.drawable)
            icon.visibility = if (nativeAd.icon == null) View.GONE else View.VISIBLE
            adView.iconView = icon
            adView.mediaView = media
            media.mediaContent = nativeAd.mediaContent
        }
        adView.headlineView = headline
        adView.bodyView = body
        adView.callToActionView = cta
        adView.setNativeAd(nativeAd)
        container.addView(adView, slotParams())
    }
}
