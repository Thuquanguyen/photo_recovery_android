package com.mobile.photo.recovery.io.ads

import com.mobile.photo.recovery.io.BuildConfig

/**
 * AdMob units. Debug always uses Google's sample test IDs so we never serve
 * live inventory from a debug APK.
 */
object AdMobConfig {
    const val PROD_APP_ID = "ca-app-pub-9626123979964133~1862472468"
    const val PROD_BANNER = "ca-app-pub-9626123979964133/2983982441"
    const val PROD_REWARDED = "ca-app-pub-9626123979964133/3283365342"
    const val PROD_APP_OPEN = "ca-app-pub-9626123979964133/2711419624"
    const val PROD_INTERSTITIAL = "ca-app-pub-9626123979964133/3122483167"
    const val PROD_NATIVE = "ca-app-pub-9626123979964133/6908583884"

    private const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
    private const val TEST_APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
    private const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_NATIVE = "ca-app-pub-3940256099942544/2247696110"

    val useTestAds: Boolean get() = BuildConfig.DEBUG

    val APP_ID: String get() = if (useTestAds) TEST_APP_ID else PROD_APP_ID
    val BANNER_UNIT_ID: String get() = if (useTestAds) TEST_BANNER else PROD_BANNER
    val REWARDED_UNIT_ID: String get() = if (useTestAds) TEST_REWARDED else PROD_REWARDED
    val APP_OPEN_UNIT_ID: String get() = if (useTestAds) TEST_APP_OPEN else PROD_APP_OPEN
    val INTERSTITIAL_UNIT_ID: String get() = if (useTestAds) TEST_INTERSTITIAL else PROD_INTERSTITIAL
    val NATIVE_UNIT_ID: String get() = if (useTestAds) TEST_NATIVE else PROD_NATIVE
}
