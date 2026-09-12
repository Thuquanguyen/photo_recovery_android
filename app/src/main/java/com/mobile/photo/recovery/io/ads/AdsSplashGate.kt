package com.mobile.photo.recovery.io.ads

/** Splash owns [open_splash]; resume App Open must not fire while splash is on screen. */
object AdsSplashGate {
    @Volatile
    var active: Boolean = false
}
