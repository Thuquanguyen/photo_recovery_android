package com.mobile.photo.recovery.io

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mobile.photo.recovery.io.ads.AppOpenAdManager
import com.mobile.photo.recovery.io.ui.nav.PhotoRecoveryNavHost
import com.mobile.photo.recovery.io.ui.theme.PhotoRecoveryTheme
import com.mobile.photo.recovery.io.util.LocaleHelper

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyStoredLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()'s own default still applies a translucent scrim over the status bar
        // on some API levels for legibility, which reads as "status bar color doesn't quite match
        // the screen behind it" — force both bars fully transparent so each screen's own
        // background/header paints through with no tint on top.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        hideSystemBars()
        AppOpenAdManager.get()?.ensureResumeCached(this)
        setContent {
            val baseContext = LocalContext.current
            // Bumped whenever the confirmed language changes so the whole app re-reads
            // the stored locale WITHOUT an Activity.recreate() — recreate() restores the
            // saved NavController back stack (still on Language) instead of restarting the
            // graph at Splash, which made Confirm appear to do nothing.
            var localeVersion by remember { mutableIntStateOf(0) }
            val localizedContext = remember(localeVersion) { LocaleHelper.applyStoredLocale(baseContext) }
            // Overriding LocalContext below (for locale) breaks rememberLauncherForActivityResult
            // (used by MediaPermission.kt) — it resolves LocalActivityResultRegistryOwner by
            // walking LocalContext's ContextWrapper chain looking for an Activity, and the
            // locale-wrapped Context returned by createConfigurationContext() isn't one, so the
            // lookup fails with "No ActivityResultRegistryOwner was provided". Re-provide it
            // explicitly, pointing at this real Activity, alongside the swapped LocalContext.
            CompositionLocalProvider(
                LocalActivityResultRegistryOwner provides this,
                LocalContext provides localizedContext
            ) {
                PhotoRecoveryTheme {
                    // enableEdgeToEdge() draws behind both the status AND navigation bars on
                    // purpose so each screen's own background color shows through behind them
                    // (matching the UI spec's edge-to-edge look) instead of a flat system color —
                    // no inset padding here at all. Each screen pads its own top content below
                    // the status bar (statusBarsPadding()) and its own bottom buttons/content
                    // above the gesture bar (navigationBarsPadding()) individually, so their
                    // backgrounds still bleed edge-to-edge on every side.
                    Surface(modifier = Modifier.fillMaxSize()) {
                        PhotoRecoveryNavHost(onLanguageApplied = { localeVersion++ })
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        AppOpenAdManager.get()?.ensureResumeCached(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    /** Hides the status bar and bottom gesture/navigation bar on entry — the user can still
     * pull either back in temporarily with a swipe (BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE),
     * which auto-hides them again once released. */
    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
