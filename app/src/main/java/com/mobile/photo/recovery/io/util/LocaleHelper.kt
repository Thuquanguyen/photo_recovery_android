package com.mobile.photo.recovery.io.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.mobile.photo.recovery.io.data.Prefs
import java.util.Locale

/** Supported languages (spec 4.2 / 6). */
data class AppLanguage(
    val code: String,
    val englishName: String,
    val nativeName: String,
    val flagEmoji: String,
    val popular: Boolean
)

object LocaleHelper {

    val languages = listOf(
        AppLanguage("en", "English", "English", "🇺🇸", true),
        AppLanguage("vi", "Vietnamese", "Tiếng Việt", "🇻🇳", true),
        AppLanguage("es", "Spanish", "Español", "🇪🇸", true),
        AppLanguage("ja", "Japanese", "日本語", "🇯🇵", true),
        AppLanguage("fr", "French", "Français", "🇫🇷", false),
        AppLanguage("de", "German", "Deutsch", "🇩🇪", false),
        AppLanguage("ko", "Korean", "한국어", "🇰🇷", false),
        AppLanguage("zh", "Chinese", "中文", "🇨🇳", false),
        AppLanguage("pt", "Portuguese", "Português", "🇵🇹", false)
    )

    /** Applies the stored language (if any) to the given base context. Used from Application/Activity. */
    fun applyStoredLocale(context: Context): Context {
        val code = Prefs.get(context).languageCode ?: return context
        return applyLocale(context, code)
    }

    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
