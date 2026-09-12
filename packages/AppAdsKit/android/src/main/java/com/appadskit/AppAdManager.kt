package com.appadskit

import android.util.Log

/** Placement + frequency + cooldown policy (host still presents the AdMob ad). */
object AppAdManager {
  private const val TAG = "AppAdsKit"

  sealed class InterstitialDecision {
    data object Skip : InterstitialDecision()
    data class Show(val unitId: String) : InterstitialDecision()
  }

  fun interstitialDecision(placement: String): InterstitialDecision {
    if (AdsKit.isVip) return InterstitialDecision.Skip
    val cfg = AdsConfig.interstitial(placement)
    if (!cfg.isShow) return InterstitialDecision.Skip
    val trigger = AdFrequencyStore.bumpInterstitialTrigger(placement)
    if (cfg.frequency > 1 && trigger % cfg.frequency != 0) {
      Log.i(TAG, "skip $placement freq=${cfg.frequency} trigger=$trigger")
      return InterstitialDecision.Skip
    }
    val last = AdFrequencyStore.lastInterstitialMs()
    val minGap = maxOf(AdPolicy.MIN_INTERSTITIAL_MS, cfg.delaySeconds * 1000L)
    if (last > 0 && System.currentTimeMillis() - last < minGap) {
      Log.i(TAG, "skip $placement min-gap=${minGap}ms")
      return InterstitialDecision.Skip
    }
    return InterstitialDecision.Show(cfg.adUnitId)
  }

  fun shouldShowAppOpen(placement: String): FullscreenPlacement? {
    if (AdsKit.isVip) return null
    val cfg = AdsConfig.appOpen(placement)
    if (!cfg.isShow) return null
    if (placement == AdPlacement.OPEN_RESUME) {
      val gap = maxOf(AdPolicy.MIN_APP_OPEN_RESUME_MS, cfg.delaySeconds * 1000L)
      if (!AdFrequencyStore.canShowResumeAppOpen(gap)) return null
    }
    return cfg
  }

  fun shouldShowRewarded(placement: String): FullscreenPlacement? {
    if (AdsKit.isVip) return null
    val cfg = AdsConfig.rewarded(placement)
    if (!cfg.isShow || !AdFrequencyStore.canShowRewarded()) return null
    return cfg
  }

  fun shouldShowNative(placement: String): NativePlacement? {
    if (AdsKit.isVip) return null
    val cfg = AdsConfig.native(placement)
    return if (cfg.isShow) cfg else null
  }

  fun shouldShowBanner(placement: String): BannerPlacement? {
    if (AdsKit.isVip) return null
    val cfg = AdsConfig.banner(placement)
    if (!cfg.isShow) return null
    if (!AdFrequencyStore.canClickBanner(AdsConfig.bannerMaxClicksPerDay)) return null
    return cfg
  }

  fun navigateAfterSupportBack(navigate: () -> Unit) {
    if (AdsKit.isVip) {
      navigate()
      return
    }
    val backs = AdFrequencyStore.bumpSupportBack()
    val every = AdPolicy.SUPPORT_BACKS_BEFORE_INTER
    if (backs % every != 0) {
      Log.i(TAG, "support back $backs — skip (every ${every}th)")
      navigate()
      return
    }
    navigate()
  }
}
