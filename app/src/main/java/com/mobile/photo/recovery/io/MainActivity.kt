package com.mobile.photo.recovery.io

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
        enableEdgeToEdge()
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
                    // enableEdgeToEdge() draws behind the status/navigation bars on purpose so
                    // each screen's own background color shows through behind them (matching the
                    // UI spec's edge-to-edge look) instead of a flat system color. Only the
                    // navigation-bar side needs padding here (buttons must clear the gesture
                    // bar); each screen pads its own top content below the status bar itself
                    // via statusBarsPadding() so its background still bleeds to the very top.
                    Surface(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
                        PhotoRecoveryNavHost(onLanguageApplied = { localeVersion++ })
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppOpenAdManager.get()?.ensureResumeCached(this)
    }
}
