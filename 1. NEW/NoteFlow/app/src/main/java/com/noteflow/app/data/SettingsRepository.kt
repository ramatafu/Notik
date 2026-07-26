package com.noteflow.app.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.Color
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ThemeMode { LIGHT, DARK, SYSTEM }

/** The four text colors available for writing notes, per user request. */
enum class NoteTextColor(val label: String, val color: Color) {
    Black("Чёрный", Color(0xFF1B1B1B)),
    Red("Красный", Color(0xFFC62828)),
    Blue("Синий", Color(0xFF1565C0)),
    Green("Зелёный", Color(0xFF2E7D32))
}

/** App language, independent of the phone's system language. */
enum class AppLanguage(val tag: String?, val nativeLabel: String) {
    SYSTEM(null, "Системный/System"),
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
        runCatching { ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name)!!) }
            .getOrDefault(ThemeMode.SYSTEM)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode

    private val _textColor = MutableStateFlow(
        runCatching { NoteTextColor.valueOf(prefs.getString(KEY_TEXT_COLOR, NoteTextColor.Black.name)!!) }
            .getOrDefault(NoteTextColor.Black)
    )
    val textColor: StateFlow<NoteTextColor> = _textColor

    /** Palette key (see ui/theme/NoteColors.kt) used as the background for newly created notes.
     *  Restricted to "White" / "Black" in Settings, per user request. */
    private val _defaultNoteColor = MutableStateFlow(prefs.getString(KEY_DEFAULT_NOTE_COLOR, "White") ?: "White")
    val defaultNoteColor: StateFlow<String> = _defaultNoteColor

    private val _language = MutableStateFlow(
        runCatching { AppLanguage.valueOf(prefs.getString(KEY_LANGUAGE, AppLanguage.SYSTEM.name)!!) }
            .getOrDefault(AppLanguage.SYSTEM)
    )
    val language: StateFlow<AppLanguage> = _language

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
        _themeMode.value = mode
    }

    fun setTextColor(color: NoteTextColor) {
        prefs.edit().putString(KEY_TEXT_COLOR, color.name).apply()
        _textColor.value = color
    }

    fun setDefaultNoteColor(paletteKey: String) {
        prefs.edit().putString(KEY_DEFAULT_NOTE_COLOR, paletteKey).apply()
        _defaultNoteColor.value = paletteKey
    }

    /** Persists the choice and immediately re-locales the app (AppCompat recreates activities as needed). */
    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.name).apply()
        _language.value = language
        val locales = if (language.tag != null) LocaleListCompat.forLanguageTags(language.tag) else LocaleListCompat.getEmptyLocaleList()
        AppCompatDelegate.setApplicationLocales(locales)
    }

    companion object {
        private const val KEY_THEME = "theme_mode"
        private const val KEY_TEXT_COLOR = "text_color"
        private const val KEY_DEFAULT_NOTE_COLOR = "default_note_color"
        private const val KEY_LANGUAGE = "language"

        @Volatile private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context).also { INSTANCE = it }
            }
    }
}
