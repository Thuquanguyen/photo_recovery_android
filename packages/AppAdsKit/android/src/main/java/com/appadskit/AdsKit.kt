package com.appadskit

import android.content.Context

object AdsKit {
  data class Settings(
    val storageKeyPrefix: String,
    val useTestAds: Boolean,
    val isVip: () -> Boolean,
    val units: AdUnitCatalog,
  )

  @Volatile
  private var settings: Settings? = null

  fun configure(context: Context, next: Settings) {
    settings = next
    AdFrequencyStore.configure(context.applicationContext, next.storageKeyPrefix)
  }

  val useTestAds: Boolean
    get() = settings?.useTestAds ?: true

  val isVip: Boolean
    get() = settings?.isVip?.invoke() ?: false

  val resolvedUnits: AdUnitCatalog
    get() {
      val catalog = settings?.units
      return if (settings?.useTestAds != false) AdUnitCatalog.googleSample else (catalog ?: AdUnitCatalog.googleSample)
    }

  fun resolveUnit(configured: String?, fallback: String): String {
    if (useTestAds) return fallback
    val id = configured?.trim().orEmpty()
    return id.ifEmpty { fallback }
  }

  fun loadFromAssets(context: Context, fileName: String) {
    AdsConfig.loadFromAssets(context, fileName)
  }

  fun applyRemoteJson(raw: String) {
    AdsConfig.applyRemoteJson(raw)
  }
}
