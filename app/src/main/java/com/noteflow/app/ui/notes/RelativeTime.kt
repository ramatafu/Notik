package com.noteflow.app.ui.notes

/** Formats a timestamp as "только что", "5 минут назад", "3 дня назад", etc. */
fun relativeTimeRu(timestampMs: Long): String {
    val diff = (System.currentTimeMillis() - timestampMs).coerceAtLeast(0)
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000

    return when {
        minutes < 1 -> "только что"
        minutes < 60 -> "$minutes ${plural(minutes, "минуту", "минуты", "минут")} назад"
        hours < 24 -> "$hours ${plural(hours, "час", "часа", "часов")} назад"
        days < 7 -> "$days ${plural(days, "день", "дня", "дней")} назад"
        days < 30 -> {
            val weeks = days / 7
            "$weeks ${plural(weeks, "неделю", "недели", "недель")} назад"
        }
        days < 365 -> {
            val months = days / 30
            "$months ${plural(months, "месяц", "месяца", "месяцев")} назад"
        }
        else -> {
            val years = days / 365
            "$years ${plural(years, "год", "года", "лет")} назад"
        }
    }
}

/** Standard Russian pluralization: one/few/many forms based on the last digit(s). */
private fun plural(count: Long, one: String, few: String, many: String): String {
    val mod100 = count % 100
    val mod10 = count % 10
    return when {
        mod100 in 11..14 -> many
        mod10 == 1L -> one
        mod10 in 2..4 -> few
        else -> many
    }
}
