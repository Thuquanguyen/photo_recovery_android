package com.appadskit

/** Named slots — CloudDeck + DroidLink compatible. */
object AdPlacement {
  const val OPEN_SPLASH = "open_splash"
  const val OPEN_RESUME = "open_resume"
  const val INTER_ONBOARDING_DONE = "inter_onboarding_done"
  const val INTER_BEFORE_RENT = "inter_before_rent"
  const val INTER_BEFORE_SHARE = "inter_before_share"
  const val INTER_SESSION_END = "inter_session_end"
  const val INTER_SUPPORT_BACK = "inter_support_back"
  const val NATIVE_HOME = "native_home"
  const val NATIVE_SHEET = "native_sheet"
  const val NATIVE_CONSOLE = "native_console"
  const val NATIVE_SETTINGS = "native_settings"
  const val NATIVE_LANGUAGE = "native_language"
  const val NATIVE_ONBOARD = "native_onboard"
  const val NATIVE_SESSIONS = "native_sessions"
  const val BANNER_SETTINGS = "banner_settings"
  const val BANNER_PANE = "banner_pane"
  const val REWARD_CONTINUE = "reward_continue"
  const val REWARD_EXTEND = "reward_extend"
}

data class FullscreenPlacement(
  val name: String,
  val adUnitId: String,
  val isShow: Boolean,
  val frequency: Int,
  val delaySeconds: Int,
)

data class NativePlacement(
  val name: String,
  val adUnitId: String,
  val isShow: Boolean,
  val form: Int,
  val isReload: Boolean,
  val reloadIntervalSeconds: Int,
)

data class BannerPlacement(
  val name: String,
  val adUnitId: String,
  val isShow: Boolean,
  val isReload: Boolean,
  val reloadAfterTimeSeconds: Int,
)

data class AdUnitCatalog(
  val appId: String,
  val appOpen: String,
  val interstitial: String,
  val rewarded: String,
  val banner: String,
  val native: String,
) {
  companion object {
    /** Official Google sample units for Android debug builds. */
    val googleSample = AdUnitCatalog(
      appId = "ca-app-pub-3940256099942544~3347511713",
      appOpen = "ca-app-pub-3940256099942544/9257395921",
      interstitial = "ca-app-pub-3940256099942544/1033173712",
      rewarded = "ca-app-pub-3940256099942544/5224354917",
      banner = "ca-app-pub-3940256099942544/6300978111",
      native = "ca-app-pub-3940256099942544/2247696110",
    )
  }
}
