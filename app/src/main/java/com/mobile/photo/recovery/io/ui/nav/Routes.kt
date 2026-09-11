package com.mobile.photo.recovery.io.ui.nav

/**
 * All app routes (spec section 3). NOTE: PREMIUM is registered here and its composable exists,
 * but by design NO screen navigates to it — matching the original app's unreachable dead screen.
 */
object Routes {
    const val SPLASH = "splash"
    const val LANGUAGE = "language"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val PHOTO_RECOVERY = "photo_recovery"
    const val QUICK_CLEAN = "quick_clean"
    const val DUPLICATE = "duplicate"
    const val SCREENSHOT = "screenshot"
    const val VAULT = "vault"
    const val SETTINGS = "settings"
    const val PREMIUM = "premium"

    /** language screen can be entered either during first-run onboarding, or later from Settings. */
    const val LANGUAGE_ARG_FROM_SETTINGS = "fromSettings"
}
