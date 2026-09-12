package com.mobile.photo.recovery.io.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Local preference storage, equivalent to the original app's app_prefs.json.
 * languageCode, onboardingCompleted, and isVip (AdsKit skips all display ads when true).
 */
class Prefs(context: Context) {
    private val sp: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var languageCode: String?
        get() = sp.getString(KEY_LANGUAGE_CODE, null)
        set(value) = sp.edit().putString(KEY_LANGUAGE_CODE, value).apply()

    var onboardingCompleted: Boolean
        get() = sp.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = sp.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var isVip: Boolean
        get() = sp.getBoolean(KEY_IS_VIP, false)
        set(value) = sp.edit().putBoolean(KEY_IS_VIP, value).apply()

    companion object {
        private const val KEY_LANGUAGE_CODE = "languageCode"
        private const val KEY_ONBOARDING_COMPLETED = "onboardingCompleted"
        private const val KEY_IS_VIP = "isVip"

        @Volatile private var instance: Prefs? = null
        fun get(context: Context): Prefs =
            instance ?: synchronized(this) {
                instance ?: Prefs(context).also { instance = it }
            }
    }
}
