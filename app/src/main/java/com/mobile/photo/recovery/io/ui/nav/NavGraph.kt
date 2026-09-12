package com.mobile.photo.recovery.io.ui.nav

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appadskit.AdPlacement
import com.appadskit.AppAdManager
import com.mobile.photo.recovery.io.ads.InterstitialAdHelper
import com.mobile.photo.recovery.io.ads.rememberHostActivity
import com.mobile.photo.recovery.io.ads.showInterstitial
import com.mobile.photo.recovery.io.data.Prefs
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

/**
 * All routes from spec section 3. NOTE: Routes.PREMIUM is registered below so the screen
 * exists and is reachable by route name, but — matching the original app exactly — no
 * composable anywhere in this graph ever navigates to it. There is no button, menu item,
 * or deep link wired to Routes.PREMIUM.
 *
 * Ad flow matches CloudDesk: splash App Open, native on language/onboarding/home/settings,
 * interstitial after onboarding, before entering a tool, when leaving a tool, and when
 * leaving Settings.
 */
@Composable
fun PhotoRecoveryNavHost(
    navController: NavHostController = rememberNavController(),
    onLanguageApplied: () -> Unit = {}
) {
    val activity = rememberHostActivity()
    val context = LocalContext.current

    fun goToTool(route: String) {
        val host = activity
        if (host != null) {
            InterstitialAdHelper.preload(host, AdPlacement.INTER_BEFORE_RENT)
            host.showInterstitial(AdPlacement.INTER_BEFORE_RENT) {
                navController.navigate(route)
            }
        } else {
            navController.navigate(route)
        }
    }

    fun leaveTool() {
        val host = activity
        if (host != null) {
            host.showInterstitial(AdPlacement.INTER_SESSION_END) {
                navController.popBackStack()
            }
        } else {
            navController.popBackStack()
        }
    }

    fun leaveSettings() {
        AppAdManager.navigateAfterSupportBack {
            val host = activity
            if (host != null) {
                host.showInterstitial(AdPlacement.INTER_SUPPORT_BACK) {
                    navController.popBackStack()
                }
            } else {
                navController.popBackStack()
            }
        }
    }

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
            LanguageScreen(onConfirm = {
                onLanguageApplied()
                val onboardingCompleted = Prefs.get(context).onboardingCompleted
                if (onboardingCompleted) {
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
            LaunchedEffect(Unit) {
                InterstitialAdHelper.preload(context, AdPlacement.INTER_BEFORE_RENT)
            }
            HomeScreen(
                onStartScan = { goToTool(Routes.PHOTO_RECOVERY) },
                onQuickClean = { goToTool(Routes.QUICK_CLEAN) },
                onDuplicate = { goToTool(Routes.DUPLICATE) },
                onScreenshot = { goToTool(Routes.SCREENSHOT) },
                onRecentlyDeleted = { goToTool(Routes.PHOTO_RECOVERY) },
                onVault = { goToTool(Routes.VAULT) },
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.PHOTO_RECOVERY) {
            SessionRoute(onLeave = ::leaveTool) { PhotoRecoveryScreen() }
        }
        composable(Routes.QUICK_CLEAN) {
            SessionRoute(onLeave = ::leaveTool) { QuickSwipeCleanScreen() }
        }
        composable(Routes.DUPLICATE) {
            SessionRoute(onLeave = ::leaveTool) { DuplicateCleanerScreen() }
        }
        composable(Routes.SCREENSHOT) {
            SessionRoute(onLeave = ::leaveTool) { ScreenshotCleanerScreen() }
        }
        composable(Routes.VAULT) {
            SessionRoute(onLeave = ::leaveTool) { VaultScreen() }
        }
        composable(Routes.SETTINGS) {
            BackHandler { leaveSettings() }
            SettingsScreen(
                onChangeLanguage = { navController.navigate(Routes.LANGUAGE) },
                onBack = { leaveSettings() }
            )
        }
        composable(Routes.PREMIUM) { PremiumScreen() }
    }
}

@Composable
private fun SessionRoute(onLeave: () -> Unit, content: @Composable () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        InterstitialAdHelper.preload(context, AdPlacement.INTER_SESSION_END)
    }
    BackHandler { onLeave() }
    content()
}
