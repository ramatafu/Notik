package com.noteflow.app.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.Color
import androidx.core.os.LocaleListCompat
import com.noteflow.app.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Only an explicit choice — no "follow system" option, per user request. */
enum class ThemeMode { LIGHT, DARK }

/** The text colors available for writing notes. labelRes points at a localized string resource. */
enum class NoteTextColor(val labelRes: Int, val color: Color) {
    Black(R.string.color_black, Color(0xFF1B1B1B)),
    Red(R.string.color_red, Color(0xFFC62828)),
    Blue(R.string.color_blue, Color(0xFF1565C0)),
    Green(R.string.color_green, Color(0xFF2E7D32)),
    White(R.string.color_white, Color(0xFFFFFFFF))
}

/** App language, independent of the phone's system language — no "system" option, per user request. */
enum class AppLanguage(val tag: String, val nativeLabel: String) {
    RUSSIAN("ru", "Русский"),
    ENGLISH("en", "English")
}

/**
 * Small SharedPreferences-backed settings store, exposed as StateFlow so the
 * UI (theme, editor, settings screen) can react to changes immediately.
 */
class SettingsRepository private constructor(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("noteflow_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        runCatching { ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.LIGHT.name)!!) }
            .getOrDefault(ThemeMode.LIGHT)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode

    private val _textColor = MutableStateFlow(
        runCatching { NoteTextColor.valueOf(prefs.getString(KEY_TEXT_COLOR, NoteTextColor.Black.name)!!) }
            .getOrDefault(NoteTextColor.Black)
    )
    val textColor: StateFlow<NoteTextColor> = _textColor

    private val _language = MutableStateFlow(
        runCatching { AppLanguage.valueOf(prefs.getString(KEY_LANGUAGE, AppLanguage.RUSSIAN.name)!!) }
            .getOrDefault(AppLanguage.RUSSIAN)
    )
    val language: StateFlow<AppLanguage> = _language

    /** Experimental feature toggle: PDF reader ("Книги" / "Books"), off by default. */
    private val _booksEnabled = MutableStateFlow(prefs.getBoolean(KEY_BOOKS_ENABLED, false))
    val booksEnabled: StateFlow<Boolean> = _booksEnabled

    fun setBooksEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BOOKS_ENABLED, enabled).apply()
        _booksEnabled.value = enabled
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
        _themeMode.value = mode
    }

    fun setTextColor(color: NoteTextColor) {
        prefs.edit().putString(KEY_TEXT_COLOR, color.name).apply()
        _textColor.value = color
    }

    /** Persists the choice and immediately re-locales the app (AppCompatActivity recreates itself). */
    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.name).apply()
        _language.value = language
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
    }

    companion object {
        private const val KEY_THEME = "theme_mode"
        private const val KEY_TEXT_COLOR = "text_color"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_BOOKS_ENABLED = "books_enabled"

        @Volatile private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context).also { INSTANCE = it }
            }
    }
}
