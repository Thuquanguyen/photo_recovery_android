package com.mobile.photo.recovery.io.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mobile.photo.recovery.io.ui.screens.DuplicateCleanerScreen
import com.mobile.photo.recovery.io.ui.screens.HomeScreen
import com.mobile.photo.recovery.io.ui.screens.LanguageScreen
import com.mobile.photo.recovery.io.ui.screens.OnboardingScreen
import com.mobile.photo.recovery.io.ui.screens.PhotoRecoveryScreen
import com.mobile.photo.recovery.io.ui.screens.PremiumScreen
import com.mobile.photo.recovery.io.ui.screens.QuickSwipeCleanScreen
import com.mobile.photo.recovery.io.ui.screens.ScreenshotCleanerScreen
import com.mobile.photo.recovery.io.ui.screens.SettingsScreen
import com.mobile.photo.recovery.io.ui.screens.SplashScreen
import com.mobile.photo.recovery.io.ui.screens.VaultScreen
import com.mobile.photo.recovery.io.data.Prefs

/**
 * All routes from spec section 3. NOTE: Routes.PREMIUM is registered below so the screen
 * exists and is reachable by route name, but — matching the original app exactly — no
 * composable anywhere in this graph ever navigates to it. There is no button, menu item,
 * or deep link wired to Routes.PREMIUM.
 */
@Composable
fun PhotoRecoveryNavHost(
    navController: NavHostController = rememberNavController(),
    onLanguageApplied: () -> Unit = {}
) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = { onboardingCompleted ->
                val destination = if (onboardingCompleted) Routes.HOME else Routes.LANGUAGE
                navController.navigate(destination) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }
        composable(Routes.LANGUAGE) {
            val context = androidx.compose.ui.platform.LocalContext.current
            LanguageScreen(onConfirm = {
                // Apply the confirmed language app-wide (see MainActivity) without an
                // Activity.recreate() — recreate() restores the saved NavController back
                // stack (still on Language) instead of restarting at Splash, which made
                // Confirm appear to do nothing. Navigate straight to the next screen instead.
                onLanguageApplied()
                val onboardingCompleted = Prefs.get(context).onboardingCompleted
                if (onboardingCompleted) {
                    // Reached from Settings' "change language" — return to Home and drop
                    // Settings/Language from the back stack instead of stacking a new Home.
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                } else {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.LANGUAGE) { inclusive = true }
                    }
                }
            })
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onGetStarted = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(
                onStartScan = { navController.navigate(Routes.PHOTO_RECOVERY) },
                onQuickClean = { navController.navigate(Routes.QUICK_CLEAN) },
                onDuplicate = { navController.navigate(Routes.DUPLICATE) },
                onScreenshot = { navController.navigate(Routes.SCREENSHOT) },
                // "Recently Deleted" is a UI alias for Photo Recovery — no separate feature (spec 4.4).
                onRecentlyDeleted = { navController.navigate(Routes.PHOTO_RECOVERY) },
                onVault = { navController.navigate(Routes.VAULT) },
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.PHOTO_RECOVERY) { PhotoRecoveryScreen() }
        composable(Routes.QUICK_CLEAN) { QuickSwipeCleanScreen() }
        composable(Routes.DUPLICATE) { DuplicateCleanerScreen() }
        composable(Routes.SCREENSHOT) { ScreenshotCleanerScreen() }
        composable(Routes.VAULT) { VaultScreen() }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onChangeLanguage = { navController.navigate(Routes.LANGUAGE) },
                onBack = { navController.popBackStack() }
            )
        }
        // Registered but unreachable from any UI element — matches spec 4.10 exactly.
        composable(Routes.PREMIUM) { PremiumScreen() }
    }
}
