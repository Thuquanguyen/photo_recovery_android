package com.mobile.photo.recovery.io.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.appadskit.AdFrequencyStore
import com.appadskit.AdPlacement
import com.appadskit.AdsConfig
import com.appadskit.AdsKit
import com.appadskit.AppAdManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.mobile.photo.recovery.io.data.Prefs
import java.util.Date

/**
 * App Open — [AdPlacement.OPEN_SPLASH] (cold start on splash) +
 * [AdPlacement.OPEN_RESUME] (background → foreground).
 *
 * Same inventory rule as CloudDesk: keep unused cache while a replacement
 * loads; no-fill never wipes a usable ad. Splash owns the first show.
 */
class AppOpenAdManager(private val application: Application) :
    DefaultLifecycleObserver,
    Application.ActivityLifecycleCallbacks {

    private val lock = Any()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var cached: AppOpenAd? = null
    private var cachedUnitId: String = ""
    private var loadedAtMs = 0L
    private var loadInFlight = false
    private var showing = false
    private var wasInBackground = false
    private var pendingResumeShow = false
    private var pendingSplashShow: (() -> Unit)? = null
    private var pendingSplashActivity: Activity? = null
    private var splashLoadRetries = 0
    private var currentActivity: Activity? = null

    private val resumeAttemptRunnable = Runnable { attemptResumeShow("delayed") }
    private val splashGiveUpRunnable = Runnable { finishPendingSplashShow("timeout") }

    fun register() {
        instance = this
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun preload(context: android.content.Context? = application, placement: String = AdPlacement.OPEN_RESUME) {
        val appCtx = context?.applicationContext ?: return
        if (Prefs.get(appCtx).isVip) return
        val cfg = AdsConfig.appOpen(placement)
        if (!cfg.isShow) return
        val keepOld: Boolean
        synchronized(lock) {
            if (showing) {
                Log.i(TAG, "preload skipped — app open is showing")
                return
            }
            keepOld = hasReady(cfg.adUnitId)
            if (loadInFlight) return
            loadInFlight = true
        }
        Log.i(TAG, "preload $placement unit=${cfg.adUnitId} keepOld=$keepOld test=${AdsKit.useTestAds}")
        val loadCtx = (context as? Activity) ?: appCtx
        AppOpenAd.load(
            loadCtx,
            cfg.adUnitId,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    val stillReady: Boolean
                    synchronized(lock) {
                        loadInFlight = false
                        stillReady = hasReady(cfg.adUnitId)
                    }
                    Log.w(TAG, "App open load fail ($placement, keep cache=$stillReady): ${error.message}")
                    if (stillReady) {
                        if (pendingSplashShow != null) {
                            mainHandler.post { tryPresentPendingSplash("fallback-cache") }
                        }
                        return
                    }
                    if (pendingSplashShow != null && splashLoadRetries < 2) {
                        splashLoadRetries += 1
                        Log.w(TAG, "splash load fail — retry $splashLoadRetries in 1.5s")
                        mainHandler.postDelayed({
                            preload(pendingSplashActivity ?: application, AdPlacement.OPEN_SPLASH)
                        }, 1_500L)
                        return
                    }
                    if (placement == AdPlacement.OPEN_SPLASH || placement == AdPlacement.OPEN_RESUME) {
                        mainHandler.post { finishPendingSplashShow("load-fail") }
                    }
                    if (pendingResumeShow && placement == AdPlacement.OPEN_RESUME) {
                        pendingResumeShow = false
                        mainHandler.removeCallbacks(resumeAttemptRunnable)
                    }
                }

                override fun onAdLoaded(ad: AppOpenAd) {
                    synchronized(lock) {
                        loadInFlight = false
                        if (showing) {
                            if (cached == null) {
                                cached = ad
                                cachedUnitId = cfg.adUnitId
                                loadedAtMs = System.currentTimeMillis()
                            }
                            return
                        }
                        cached = ad
                        cachedUnitId = cfg.adUnitId
                        loadedAtMs = System.currentTimeMillis()
                    }
                    Log.i(TAG, "App open loaded ($placement) — replaced unused cache")
                    if (pendingSplashShow != null) {
                        mainHandler.post { tryPresentPendingSplash("onLoaded") }
                    }
                    if (pendingResumeShow && placement == AdPlacement.OPEN_RESUME) {
                        mainHandler.post { attemptResumeShow("onLoaded") }
                    }
                }
            },
        )
    }

    fun ensureResumeCached(context: android.content.Context) {
        if (Prefs.get(context).isVip) return
        val cfg = AdsConfig.appOpen(AdPlacement.OPEN_RESUME)
        if (!cfg.isShow) return
        preload(context, AdPlacement.OPEN_RESUME)
    }

    fun showSplash(activity: Activity, onFinished: () -> Unit) {
        if (Prefs.get(activity).isVip) {
            onFinished()
            return
        }
        val cfg = AppAdManager.shouldShowAppOpen(AdPlacement.OPEN_SPLASH)
        if (cfg == null) {
            Log.i(TAG, "open_splash disabled via JSON")
            onFinished()
            return
        }
        val ready = synchronized(lock) { if (hasReady(cfg.adUnitId)) cached else null }
        if (ready != null) {
            present(activity, ready) {
                AdFrequencyStore.markAppOpenShown()
                onFinished()
            }
            return
        }
        pendingSplashActivity = activity
        pendingSplashShow = onFinished
        splashLoadRetries = 0
        mainHandler.removeCallbacks(splashGiveUpRunnable)
        mainHandler.postDelayed(splashGiveUpRunnable, SPLASH_SHOW_GRACE_MS)
        preload(activity, AdPlacement.OPEN_SPLASH)
        Log.i(TAG, "splash show: waiting up to ${SPLASH_SHOW_GRACE_MS}ms for fill")
    }

    private fun tryPresentPendingSplash(reason: String) {
        val activity = pendingSplashActivity
        val onFinished = pendingSplashShow ?: return
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            finishPendingSplashShow("activity-gone")
            return
        }
        val cfg = AdsConfig.appOpen(AdPlacement.OPEN_SPLASH)
        val ad = synchronized(lock) { if (hasReady(cfg.adUnitId)) cached else null } ?: return
        pendingSplashShow = null
        pendingSplashActivity = null
        mainHandler.removeCallbacks(splashGiveUpRunnable)
        Log.i(TAG, "splash show ($reason)")
        present(activity, ad) {
            AdFrequencyStore.markAppOpenShown()
            onFinished()
        }
    }

    private fun finishPendingSplashShow(reason: String) {
        val onFinished = pendingSplashShow ?: return
        pendingSplashShow = null
        pendingSplashActivity = null
        mainHandler.removeCallbacks(splashGiveUpRunnable)
        Log.i(TAG, "splash show give up ($reason)")
        onFinished()
    }

    override fun onStop(owner: LifecycleOwner) {
        if (FullScreenAdGate.isBusy()) {
            Log.d(TAG, "process ON_STOP ignored — fullscreen ad")
            return
        }
        wasInBackground = true
    }

    override fun onStart(owner: LifecycleOwner) {
        if (!wasInBackground) {
            return
        }
        wasInBackground = false
        pendingResumeShow = true
        mainHandler.removeCallbacks(resumeAttemptRunnable)
        mainHandler.post { attemptResumeShow("onStart") }
        mainHandler.postDelayed(resumeAttemptRunnable, 250L)
        mainHandler.postDelayed(resumeAttemptRunnable, 800L)
        mainHandler.postDelayed({
            if (!pendingResumeShow) return@postDelayed
            attemptResumeShow("grace-end")
            if (pendingResumeShow) {
                pendingResumeShow = false
                Log.i(TAG, "resume give up — ad not ready within ${RESUME_LOAD_GRACE_MS}ms")
            }
        }, RESUME_LOAD_GRACE_MS)
    }

    private fun attemptResumeShow(reason: String) {
        if (!pendingResumeShow) return
        val activity = currentActivity
        if (activity == null) return
        if (AdsSplashGate.active) {
            Log.d(TAG, "resume skip ($reason): splash")
            return
        }
        if (activity.isFinishing || activity.isDestroyed) return
        if (Prefs.get(application).isVip) {
            pendingResumeShow = false
            return
        }
        if (AppAdManager.shouldShowAppOpen(AdPlacement.OPEN_RESUME) == null) {
            pendingResumeShow = false
            return
        }
        if (showing || FullScreenAdGate.isBusy()) return
        if (FullScreenAdGate.consumeSkipAppOpen()) {
            pendingResumeShow = false
            return
        }
        val cfg = AdsConfig.appOpen(AdPlacement.OPEN_RESUME)
        val ad = synchronized(lock) { if (hasReady(cfg.adUnitId)) cached else null }
        if (ad == null) {
            preload(application, AdPlacement.OPEN_RESUME)
            return
        }
        pendingResumeShow = false
        mainHandler.removeCallbacks(resumeAttemptRunnable)
        Log.i(TAG, "resume show ($reason)")
        present(activity, ad) {
            AdFrequencyStore.markAppOpenShown()
            preload(application, AdPlacement.OPEN_RESUME)
        }
    }

    private fun present(activity: Activity, ad: AppOpenAd, onFinished: () -> Unit) {
        val go = Runnable {
            if (activity.isFinishing || activity.isDestroyed) {
                onFinished()
                return@Runnable
            }
            if (!FullScreenAdGate.acquire()) {
                Log.w(TAG, "present blocked: FullScreenAdGate busy")
                onFinished()
                return@Runnable
            }
            synchronized(lock) {
                showing = true
                if (cached === ad) {
                    cached = null
                    cachedUnitId = ""
                }
            }
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    showing = false
                    FullScreenAdGate.release()
                    onFinished()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    showing = false
                    FullScreenAdGate.release()
                    Log.w(TAG, "App open show fail: ${error.message} code=${error.code}")
                    onFinished()
                }
            }
            Log.i(TAG, "present App Open on ${activity.javaClass.simpleName} focused=${activity.hasWindowFocus()}")
            ad.show(activity)
        }
        if (activity.hasWindowFocus()) {
            go.run()
        } else {
            activity.window.decorView.post(go)
        }
    }

    private fun hasReady(unitId: String): Boolean {
        if (cached == null) return false
        if (cachedUnitId.isNotEmpty() && cachedUnitId != unitId) return false
        return Date().time - loadedAtMs < AD_EXPIRY_MS
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        if (pendingResumeShow) {
            mainHandler.post { attemptResumeShow("onActivityResumed") }
        }
    }
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity === activity) currentActivity = null
    }

    companion object {
        private const val TAG = "AppOpenAd"
        private const val AD_EXPIRY_MS = 4L * 60L * 60L * 1000L
        private const val RESUME_LOAD_GRACE_MS = 4_000L
        private const val SPLASH_SHOW_GRACE_MS = 8_000L

        @Volatile
        private var instance: AppOpenAdManager? = null

        fun get(): AppOpenAdManager? = instance
    }
}
