package com.mobile.photo.recovery.io.ads

import android.content.Context
import com.appadskit.AdUnitCatalog
import com.appadskit.AdsKit
import com.mobile.photo.recovery.io.BuildConfig
import com.mobile.photo.recovery.io.data.Prefs

/** Host wiring: JSON scenario + DEBUG sample units (same kit as CloudDesk). */
object PhotoAds {
    fun bootstrap(context: Context) {
        val app = context.applicationContext
        AdsKit.configure(
            app,
            AdsKit.Settings(
                storageKeyPrefix = "photo.ads",
                useTestAds = BuildConfig.DEBUG,
                isVip = { Prefs.get(app).isVip },
                units = AdUnitCatalog(
                    appId = AdMobConfig.PROD_APP_ID,
                    appOpen = AdMobConfig.PROD_APP_OPEN,
                    interstitial = AdMobConfig.PROD_INTERSTITIAL,
                    rewarded = AdMobConfig.PROD_REWARDED,
                    banner = AdMobConfig.PROD_BANNER,
                    native = AdMobConfig.PROD_NATIVE,
                ),
            ),
        )
        AdsKit.loadFromAssets(app, "ads_config_android.json")
    }
}
