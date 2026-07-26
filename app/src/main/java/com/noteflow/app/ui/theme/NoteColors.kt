package com.noteflow.app.ui.theme

import androidx.compose.ui.graphics.Color

/** Named palette applied to note backgrounds — keys are stored in Note.color.
 *  "Default" is intentionally NOT pure white — notes should never render on
 *  a stark white background (see settings for choosing the default for new notes). */
val NoteColorPalette: Map<String, Color> = linkedMapOf(
    "Default" to Color(0xFFF3F1EC),
    "Coral" to Color(0xFFF28B82),
    "Peach" to Color(0xFFFBBC04),
    "Sand" to Color(0xFFFFF475),
    "Sage" to Color(0xFFCCFF90),
    "Mint" to Color(0xFFA7FFEB),
    "Fog" to Color(0xFFCBF0F8),
    "Storm" to Color(0xFFAECBFA),
    "Dusk" to Color(0xFFD7AEFB),
    "Blossom" to Color(0xFFFDCFE8),
    "White" to Color(0xFFFFFFFF),
    "Black" to Color(0xFF1B1B1B)
)

/** Human-readable Russian names for every palette key — used anywhere colors are shown in settings. */
val NoteColorLabels: Map<String, String> = mapOf(
    "Default" to "Бежевый",
    "Coral" to "Коралловый",
    "Peach" to "Персиковый",
    "Sand" to "Песочный",
    "Sage" to "Шалфей",
    "Mint" to "Мятный",
    "Fog" to "Туманный",
    "Storm" to "Грозовой",
    "Dusk" to "Сумеречный",
    "Blossom" to "Цветочный",
    "White" to "Белый",
    "Black" to "Чёрный"
)

/** The only two options offered in Settings for the default background of new notes. */
val DefaultNoteBackgroundOptions: List<String> = listOf("White", "Black")

fun colorForKey(key: String): Color = NoteColorPalette[key] ?: NoteColorPalette.getValue("Default")

