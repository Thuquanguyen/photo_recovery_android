package com.mobile.photo.recovery.io.ads

/**
 * Ensures only one fullscreen ad (reward / interstitial / app-open) is visible at a time,
 * and suppresses back-to-back stacking after reward dismiss (app-open / interstitial).
 */
object FullScreenAdGate {
    private val lock = Any()
    private var showing = false
    private var skipNextAppOpen = false
    private var skipNextInterstitial = false
    private var pendingReward = false

    fun isBusy(): Boolean = synchronized(lock) { showing || pendingReward }

    fun beginRewardFlow(): Boolean = synchronized(lock) {
        if (showing || pendingReward) return false
        pendingReward = true
        skipNextAppOpen = true
        true
    }

    fun endRewardFlow() = synchronized(lock) {
        pendingReward = false
    }

    fun acquire(): Boolean = synchronized(lock) {
        if (showing) return false
        showing = true
        pendingReward = false
        skipNextAppOpen = true
        true
    }

    fun release() = synchronized(lock) {
        showing = false
        pendingReward = false
        skipNextAppOpen = true
    }

    fun markSkipNextInterstitial() = synchronized(lock) {
        skipNextInterstitial = true
        skipNextAppOpen = true
    }

    fun consumeSkipInterstitial(): Boolean = synchronized(lock) {
        if (!skipNextInterstitial) return false
        skipNextInterstitial = false
        true
    }

    fun consumeSkipAppOpen(): Boolean = synchronized(lock) {
        if (!skipNextAppOpen) return false
        skipNextAppOpen = false
        true
    }
}
