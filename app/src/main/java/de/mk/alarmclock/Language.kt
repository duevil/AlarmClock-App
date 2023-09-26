package de.mk.alarmclock

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat

enum class Language(val code: String, @StringRes private val textId: Int) {
    SYSTEM("system", R.string.language_system),
    ENGLISH("en", R.string.language_english),
    GERMAN("de", R.string.language_german),
    ;

    val text @Composable get() = stringResource(textId)

    companion object {
        val LocaleListCompat.language: Language
            get() = get(0)?.language?.let { l ->
                values().firstOrNull { l.contains(it.code, ignoreCase = true) }
            } ?: SYSTEM
        var appLocalListCompat: LocaleListCompat
            get() = AppCompatDelegate.getApplicationLocales()
            set(value) = AppCompatDelegate.setApplicationLocales(value)
    }
}