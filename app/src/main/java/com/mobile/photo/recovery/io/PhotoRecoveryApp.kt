package com.mobile.photo.recovery.io

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.mobile.photo.recovery.io.util.LocaleHelper

class PhotoRecoveryApp : Application(), ImageLoaderFactory {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.applyStoredLocale(base))
    }

    // Registers VideoFrameDecoder so every AsyncImage in the app (Photo Recovery, Quick Swipe
    // Clean, etc.) can render a real video-frame thumbnail for video MediaStore items instead of
    // failing to decode them — Coil's built-in decoders only handle static image formats.
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()
}
